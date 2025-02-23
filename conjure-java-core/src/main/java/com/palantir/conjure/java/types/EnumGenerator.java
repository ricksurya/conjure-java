/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.java.types;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.annotations.Immutable;
import com.palantir.conjure.java.ConjureAnnotations;
import com.palantir.conjure.java.Options;
import com.palantir.conjure.java.util.Javadoc;
import com.palantir.conjure.java.util.Packages;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.logsafe.Safe;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;

public final class EnumGenerator {

    private static final String VALUE_PARAMETER = "value";
    private static final String STRING_PARAMETER = "string";
    private static final String VISIT_METHOD_NAME = "visit";
    private static final String VISIT_UNKNOWN_METHOD_NAME = "visitUnknown";
    private static final TypeVariableName TYPE_VARIABLE = TypeVariableName.get("T");

    private EnumGenerator() {}

    public static JavaFile generateEnumType(EnumDefinition typeDef, Options options) {
        com.palantir.conjure.spec.TypeName prefixedTypeName =
                Packages.getPrefixedName(typeDef.getTypeName(), options.packagePrefix());
        ClassName thisClass = ClassName.get(prefixedTypeName.getPackage(), prefixedTypeName.getName());
        ClassName enumClass = ClassName.get(prefixedTypeName.getPackage(), prefixedTypeName.getName(), "Value");
        ClassName visitorClass = ClassName.get(
                prefixedTypeName.getPackage(), typeDef.getTypeName().getName(), "Visitor");

        return JavaFile.builder(
                        prefixedTypeName.getPackage(), createSafeEnum(typeDef, thisClass, enumClass, visitorClass))
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static TypeSpec createSafeEnum(
            EnumDefinition typeDef, ClassName thisClass, ClassName enumClass, ClassName visitorClass) {
        TypeSpec.Builder wrapper = TypeSpec.classBuilder(typeDef.getTypeName().getName())
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(EnumGenerator.class))
                .addAnnotation(Safe.class)
                .addAnnotation(Immutable.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addType(createEnum(enumClass, typeDef.getValues(), true))
                .addType(createVisitor(visitorClass, typeDef.getValues()))
                .addField(enumClass, VALUE_PARAMETER, Modifier.PRIVATE, Modifier.FINAL)
                .addField(ClassName.get(String.class), STRING_PARAMETER, Modifier.PRIVATE, Modifier.FINAL)
                .addFields(createConstants(typeDef.getValues(), thisClass, enumClass))
                .addField(createValuesList(thisClass, typeDef.getValues()))
                .addMethod(createConstructor(enumClass))
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(enumClass)
                        .addStatement("return this.value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addAnnotation(JsonValue.class)
                        .returns(ClassName.get(String.class))
                        .addStatement("return this.string")
                        .build())
                .addMethod(createEquals(thisClass))
                .addMethod(createHashCode())
                .addMethod(createValueOf(thisClass, typeDef.getValues()))
                .addMethod(generateAcceptVisitMethod(visitorClass, typeDef.getValues()))
                .addMethod(createValues(thisClass));

        typeDef.getDocs().ifPresent(docs -> wrapper.addJavadoc("$L<p>\n", Javadoc.render(docs)));

        wrapper.addJavadoc(
                "This class is used instead of a native enum to support unknown values.\n"
                        + "Rather than throw an exception, the {@link $1T#valueOf} method defaults to a new "
                        + "instantiation of\n{@link $1T} where {@link $1T#get} will return {@link $2T#UNKNOWN}.\n"
                        + "<p>\n"
                        + "For example, {@code $1T.valueOf(\"corrupted value\").get()} will return "
                        + "{@link $2T#UNKNOWN},\nbut {@link $1T#toString} will return \"corrupted value\".\n"
                        + "<p>\n"
                        + "There is no method to access all instantiations of this class, since they cannot be known "
                        + "at compile time.\n",
                thisClass,
                enumClass);

        return wrapper.build();
    }

    private static Iterable<FieldSpec> createConstants(
            Iterable<EnumValueDefinition> values, ClassName thisClass, ClassName enumClass) {
        return Iterables.transform(values, v -> {
            FieldSpec.Builder fieldSpec = FieldSpec.builder(
                            thisClass, v.getValue(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(
                            CodeBlock.of("new $1T($2T.$3N, $4S)", thisClass, enumClass, v.getValue(), v.getValue()));
            Javadoc.render(v.getDocs(), v.getDeprecated()).ifPresent(docs -> fieldSpec.addJavadoc("$L", docs));
            v.getDeprecated().ifPresent(_deprecated -> fieldSpec.addAnnotation(Deprecated.class));
            return fieldSpec.build();
        });
    }

    private static TypeSpec createEnum(ClassName enumClass, Iterable<EnumValueDefinition> values, boolean withUnknown) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(enumClass.simpleName())
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(EnumGenerator.class))
                .addModifiers(Modifier.PUBLIC);
        for (EnumValueDefinition value : values) {
            TypeSpec.Builder anonymousClassBuilder = TypeSpec.anonymousClassBuilder("");
            Javadoc.render(value.getDocs(), value.getDeprecated())
                    .ifPresent(docs -> anonymousClassBuilder.addJavadoc("$L", docs));
            value.getDeprecated().ifPresent(_deprecation -> anonymousClassBuilder.addAnnotation(Deprecated.class));
            enumBuilder.addEnumConstant(value.getValue(), anonymousClassBuilder.build());
        }
        if (withUnknown) {
            enumBuilder.addEnumConstant("UNKNOWN");
        } else {
            enumBuilder.addMethod(MethodSpec.methodBuilder("fromString")
                    .addJavadoc("$L", "Preferred, case-insensitive constructor for string-to-enum conversion.\n")
                    .addAnnotation(ConjureAnnotations.delegatingJsonCreator())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.get(String.class), "value")
                    .addStatement("return $T.valueOf(value.toUpperCase($T.ROOT))", enumClass, Locale.class)
                    .returns(enumClass)
                    .build());
        }
        return enumBuilder.build();
    }

    private static TypeSpec createVisitor(ClassName visitorClass, Iterable<EnumValueDefinition> values) {
        return TypeSpec.interfaceBuilder(visitorClass.simpleName())
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(EnumGenerator.class))
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TYPE_VARIABLE)
                .addMethods(generateMemberVisitMethods(values))
                .addMethod(MethodSpec.methodBuilder(VISIT_UNKNOWN_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(String.class, "unknownValue")
                        .returns(TYPE_VARIABLE)
                        .build())
                .build();
    }

    private static List<MethodSpec> generateMemberVisitMethods(Iterable<EnumValueDefinition> values) {
        ImmutableList.Builder<MethodSpec> methods = ImmutableList.builder();
        for (EnumValueDefinition value : values) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(getVisitorMethodName(value))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(TYPE_VARIABLE);
            Javadoc.render(value.getDocs(), value.getDeprecated())
                    .ifPresent(docs -> methodSpecBuilder.addJavadoc("$L", docs));
            value.getDeprecated().ifPresent(_deprecated -> methodSpecBuilder.addAnnotation(Deprecated.class));
            methods.add(methodSpecBuilder.build());
        }
        return methods.build();
    }

    private static MethodSpec generateAcceptVisitMethod(ClassName visitorClass, List<EnumValueDefinition> values) {
        CodeBlock.Builder switchBlock = CodeBlock.builder();
        switchBlock.beginControlFlow("switch (value)");
        for (EnumValueDefinition value : values) {
            switchBlock
                    .add("case $N:\n", value.getValue())
                    .addStatement("return visitor.$L()", getVisitorMethodName(value));
        }
        switchBlock
                .add("default:\n")
                .addStatement("return visitor.$1L(string)", VISIT_UNKNOWN_METHOD_NAME)
                .endControlFlow();
        ParameterizedTypeName parameterizedVisitorClass = ParameterizedTypeName.get(visitorClass, TYPE_VARIABLE);
        boolean anyDeprecatedValues = values.stream()
                .anyMatch(definition -> definition.getDeprecated().isPresent());
        return MethodSpec.methodBuilder("accept")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(parameterizedVisitorClass, "visitor")
                        .build())
                .addTypeVariable(TYPE_VARIABLE)
                .returns(TYPE_VARIABLE)
                .addCode(switchBlock.build())
                .addAnnotations(
                        anyDeprecatedValues
                                ? ImmutableList.of(AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "deprecation")
                                        .build())
                                : ImmutableList.of())
                .build();
    }

    private static String getVisitorMethodName(EnumValueDefinition definition) {
        return VISIT_METHOD_NAME + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, definition.getValue());
    }

    private static MethodSpec createConstructor(ClassName enumClass) {
        // Note: We generate a two arg constructor that handles both known
        // and unknown variants instead of using separate constructors to avoid
        // jackson's behavior of preferring single string constructors for key
        // deserializers over static factories.
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(enumClass, "value")
                .addParameter(ClassName.get(String.class), "string")
                .addStatement("this.value = value")
                .addStatement("this.string = string")
                .build();
    }

    private static MethodSpec createValueOf(ClassName thisClass, List<EnumValueDefinition> values) {
        ParameterSpec param = ParameterSpec.builder(ClassName.get(String.class), "value")
                .addAnnotation(Nonnull.class)
                .addAnnotation(Safe.class)
                .build();

        CodeBlock.Builder parser = CodeBlock.builder().beginControlFlow("switch (upperCasedValue)");
        for (EnumValueDefinition value : values) {
            parser.add("case $S:\n", value.getValue())
                    .indent()
                    .addStatement("return $L", value.getValue())
                    .unindent();
        }
        parser.add("default:\n")
                .indent()
                .addStatement("return new $T(Value.UNKNOWN, upperCasedValue)", thisClass)
                .unindent()
                .endControlFlow();
        boolean anyDeprecatedValues = values.stream()
                .anyMatch(definition -> definition.getDeprecated().isPresent());
        return MethodSpec.methodBuilder("valueOf")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(thisClass)
                .addAnnotation(ConjureAnnotations.delegatingJsonCreator())
                .addAnnotations(
                        anyDeprecatedValues
                                ? ImmutableList.of(AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "deprecation")
                                        .build())
                                : ImmutableList.of())
                .addParameter(param)
                .addStatement("$L", Expressions.requireNonNull(param.name, param.name + " cannot be null"))
                // uppercase param for backwards compatibility
                .addStatement("String upperCasedValue = $N.toUpperCase($T.ROOT)", param, Locale.class)
                .addCode(parser.build())
                .build();
    }

    private static FieldSpec createValuesList(ClassName thisClass, List<EnumValueDefinition> values) {
        CodeBlock arrayValues = values.stream()
                .map(value -> CodeBlock.of("$L", value.getValue()))
                .collect(CodeBlock.joining(", "));
        boolean anyDeprecatedValues = values.stream()
                .anyMatch(definition -> definition.getDeprecated().isPresent());
        return FieldSpec.builder(
                        ParameterizedTypeName.get(ClassName.get(List.class), thisClass),
                        "values",
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL)
                .initializer(CodeBlock.of(
                        "$T.unmodifiableList($T.asList($L))", Collections.class, Arrays.class, arrayValues))
                .addAnnotations(
                        anyDeprecatedValues
                                ? ImmutableList.of(AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "deprecation")
                                        .build())
                                : ImmutableList.of())
                .build();
    }

    private static MethodSpec createValues(ClassName thisClass) {
        return MethodSpec.methodBuilder("values")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), thisClass))
                .addStatement("return values")
                .build();
    }

    private static MethodSpec createEquals(TypeName thisClass) {
        ParameterSpec other = ParameterSpec.builder(ClassName.OBJECT, "other").build();
        return MethodSpec.methodBuilder("equals")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(other)
                .returns(TypeName.BOOLEAN)
                .addStatement(
                        "return (this == $1N) || ($1N instanceof $2T && this.string.equals((($2T) $1N).string))",
                        other,
                        thisClass)
                .build();
    }

    private static MethodSpec createHashCode() {
        return MethodSpec.methodBuilder("hashCode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addStatement("return this.string.hashCode()")
                .build();
    }
}
