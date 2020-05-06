package de.adesso.objectfieldcoverage.core.util.exception;

/**
 * Runtime exception thrown when a target method of a given test case does not match the
 * allowed signature of a tested method.
 *
 * @see de.adesso.objectfieldcoverage.core.util.ExecutableUtil
 */
public class IllegalMethodSignatureException extends RuntimeException {

    /**
     *
     * @param message
     *          The message of the exception, not {@code null}.
     */
    public IllegalMethodSignatureException(String message) {
        super(message);
    }

}
