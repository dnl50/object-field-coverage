package de.adesso.objectfieldcoverage.core.processor.exception;

/**
 * Runtime exception thrown when a target method of a given test case cannot be found in the
 * current Spoon model.
 */
public class TargetMethodNotFoundException extends RuntimeException {

    /**
     *
     * @param identifier
     *          The method identifier for which no method was found in the current
     *          Spoon model, not {@code null}.
     */
    public TargetMethodNotFoundException(String identifier) {
        super(String.format("Method '%s' not found in current model!", identifier));
    }

}
