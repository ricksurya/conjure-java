package test.prefix.com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.palantir.ri.ResourceIdentifier;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.processing.Generated;

@JsonDeserialize(builder = RidExample.Builder.class)
@Generated("com.palantir.conjure.java.types.BeanGenerator")
public final class RidExample {
    private final ResourceIdentifier ridValue;

    private RidExample(ResourceIdentifier ridValue) {
        validateFields(ridValue);
        this.ridValue = ridValue;
    }

    @JsonProperty("ridValue")
    public ResourceIdentifier getRidValue() {
        return this.ridValue;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof RidExample && equalTo((RidExample) other));
    }

    private boolean equalTo(RidExample other) {
        return this.ridValue.equals(other.ridValue);
    }

    @Override
    public int hashCode() {
        return this.ridValue.hashCode();
    }

    @Override
    public String toString() {
        return "RidExample{ridValue: " + ridValue + '}';
    }

    public static RidExample of(ResourceIdentifier ridValue) {
        return builder().ridValue(ridValue).build();
    }

    private static void validateFields(ResourceIdentifier ridValue) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, ridValue, "ridValue");
        if (missingFields != null) {
            throw new SafeIllegalArgumentException(
                    "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(1);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        boolean _buildInvoked;

        private ResourceIdentifier ridValue;

        private Builder() {}

        public Builder from(RidExample other) {
            checkNotBuilt();
            ridValue(other.getRidValue());
            return this;
        }

        @JsonSetter("ridValue")
        public Builder ridValue(@Nonnull ResourceIdentifier ridValue) {
            checkNotBuilt();
            this.ridValue = Preconditions.checkNotNull(ridValue, "ridValue cannot be null");
            return this;
        }

        public RidExample build() {
            checkNotBuilt();
            this._buildInvoked = true;
            return new RidExample(ridValue);
        }

        private void checkNotBuilt() {
            Preconditions.checkState(!_buildInvoked, "Build has already been called");
        }
    }
}
