package de.adesso.objectfieldcoverage.core.util.exception;

/**
 * Runtime exception thrown when a service provider could not be located.
 */
public class NoServiceProviderFoundException extends RuntimeException {

    /**
     *
     * @param serviceType
     *          The type for which no service provider could be located, not {@code null}.
     */
    public NoServiceProviderFoundException(Class<?> serviceType) {
        super(String.format("No service provider for type '%s' found!", serviceType.getName()));
    }

}
