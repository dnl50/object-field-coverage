package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.core.util.exception.NoServiceProviderFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceLoaderUtil {

    /**
     *
     * @param serviceType
     *          The type of the service which should be loaded using the Java {@link ServiceLoader},
     *          not {@code null}.
     *
     * @param callerType
     *          The type of which the class loader should be used, not {@code null}.
     *
     * @param <T>
     *          The type of the service.
     *
     * @return
     *          A set containing all service instances which were found using the class loader
     *          of the given {@code callerType}. This list may be empty in case no services of
     *          the specified type were found.
     */
    public static <T> Set<T> loadServices(Class<T> serviceType, Class<?> callerType) {
        Objects.requireNonNull(serviceType, "serviceType cannot be null!");
        Objects.requireNonNull(callerType, "callerType cannot be null!");

        var serviceLoader = ServiceLoader.load(serviceType, callerType.getClassLoader());

        return serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param serviceType
     *          The type of the service which should be loaded using the Java {@link ServiceLoader},
     *          not {@code null}.
     *
     * @param <T>
     *          The type of the service.
     *
     * @return
     *          A set containing all service instances which were found using the class loader
     *          of {@code this} util class. This list may be empty in case no services of
     *          the specified type were found.
     *
     * @see #loadServices(Class, Class)
     */
    public static <T> Set<T> loadServices(Class<T> serviceType) {
        var thisClass = ServiceLoaderUtil.class;
        return loadServices(serviceType, thisClass);
    }

    /**
     *
     * @param serviceType
     *          The type of the service which should be loaded using the Java {@link ServiceLoader},
     *          not {@code null}.
     *
     * @param callerType
     *          The type of which the class loader should be used, not {@code null}.
     *
     * @param <T>
     *          The type of the service.
     *
     * @return
     *          A single service instance which was found using the class loader
     *          of the given {@code callerType}.
     *
     * @throws NoServiceProviderFoundException
     *          When no service instance was found using the given {@code callerType}'s class loader.
     */
    public static <T> T loadService(Class<T> serviceType, Class<?> callerType) throws NoServiceProviderFoundException {
        Objects.requireNonNull(serviceType, "serviceType cannot be null!");
        Objects.requireNonNull(callerType, "callerType cannot be null!");

        var serviceLoader = ServiceLoader.load(serviceType, callerType.getClassLoader());

        return serviceLoader.findFirst()
                .orElseThrow(() -> new NoServiceProviderFoundException(serviceType));
    }

    /**
     *
     * @param serviceType
     *          The type of the service which should be loaded using the Java {@link ServiceLoader},
     *          not {@code null}.
     *
     * @param <T>
     *          The type of the service.
     *
     * @return
     *          A single service instance which was found using the class loader
     *          of {@code this} util class.
     *
     * @throws NoServiceProviderFoundException
     *          When no service instance was found using the class loader of {@code this} util
     *          class.
     */
    public static <T> T loadService(Class<T> serviceType) throws NoServiceProviderFoundException {
        var thisClass = ServiceLoaderUtil.class;
        return loadService(serviceType, thisClass);
    }

}
