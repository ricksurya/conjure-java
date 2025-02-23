package com.palantir.another;

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.RemoteException;
import com.palantir.conjure.java.api.errors.ServiceException;
import com.palantir.logsafe.Preconditions;
import javax.annotation.processing.Generated;

@Generated("com.palantir.conjure.java.types.ErrorGenerator")
public final class ConjureJavaOtherErrors {
    /**
     * Failed to compile Conjure definition to Java code.
     */
    public static final ErrorType JAVA_COMPILATION_FAILED =
            ErrorType.create(ErrorType.Code.INTERNAL, "ConjureJavaOther:JavaCompilationFailed");

    private ConjureJavaOtherErrors() {}

    public static ServiceException javaCompilationFailed() {
        return new ServiceException(JAVA_COMPILATION_FAILED);
    }

    public static ServiceException javaCompilationFailed(Throwable cause) {
        return new ServiceException(JAVA_COMPILATION_FAILED, cause);
    }

    /**
     * Throws a {@link ServiceException} of type JavaCompilationFailed when {@code shouldThrow} is true.
     * @param shouldThrow Cause the method to throw when true
     */
    public static void throwIfJavaCompilationFailed(boolean shouldThrow) {
        if (shouldThrow) {
            throw javaCompilationFailed();
        }
    }

    /**
     * Returns true if the {@link RemoteException} is named ConjureJavaOther:JavaCompilationFailed
     */
    public static boolean isJavaCompilationFailed(RemoteException remoteException) {
        Preconditions.checkNotNull(remoteException, "remote exception must not be null");
        return JAVA_COMPILATION_FAILED.name().equals(remoteException.getError().errorName());
    }
}
