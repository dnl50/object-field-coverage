package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.api.Order;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.platform.commons.util.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClasspathUtils {

    /**
     * The base package where implementations are searched in.
     */
    private static final String BASE_PACKAGE = "de.adesso";

    /**
     * Reusable Reflections instance for increased performance. Must be initialized once.
     */
    private static Reflections reflections;

    /**
     *
     * @param type
     *          The type to find the implementing / extending classes of, not {@code null}.
     *
     * @param <T>
     *          The type of the class.
     *
     * @return
     *          A list a single instance of each concrete class implementing/extending the given {@code type} which
     *          declares a public no-arg constructor inm the {@value #BASE_PACKAGE} package. The list is in descending order
     *          according to the value specified in the {@link Order} annotation. {@link Order#DEFAULT} used when
     *          the type is not annotated.
     */
    public static <T> List<T> loadClassesImplementingInterfaceOrExtendingClass(Class<T> type) {
        Objects.requireNonNull(type, "The given type cannot be null!");

        var subtypes = initReflections().getSubTypesOf(type);

        List<T> foundTypes = subtypes.stream()
                .filter(ClasspathUtils::isConcreteClass)
                .filter(ClasspathUtils::hasPublicNoArgConstructor)
                .map(ClasspathUtils::getOrderPair)
                .sorted(Comparator.comparing(Pair::getRight, (i1, i2) -> -1 * Integer.compare(i1, i2)))
                .map(Pair::getKey)
                .map(ClasspathUtils::instantiateWithPublicNoArgConstructor)
                .collect(Collectors.toList());

        log.debug("Found {} concrete subtypes of {}!", foundTypes.size(), type.getName());

        return foundTypes;
    }

    /**
     *
     * @param type
     *          The type to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} is neither an interface nor an abstract class. {@code false}
     *          is returned otherwise.
     */
    private static boolean isConcreteClass(Class<?> type) {
        var typeModifiers = type.getModifiers();
        return !Modifier.isInterface(typeModifiers) && !Modifier.isAbstract(typeModifiers);
    }

    /**
     *
     * @param type
     *          The class instance of the type, not {@code null}.
     *
     * @param <T>
     *          The type of the class.
     *
     * @return
     *          A pair which maps the given {@code type} to its order value. Uses {@link Order#DEFAULT}
     *          in case the given type is not annotated with {@link Order}.
     */
    private static <T> Pair<Class<T>, Integer> getOrderPair(Class<T> type) {
        var orderAnnotation = type.getAnnotation(Order.class);
        var order = orderAnnotation != null ? orderAnnotation.value() : Order.DEFAULT;

        return Pair.of(type, order);
    }

    /**
     *
     * @param type
     *          The type to get the public no-arg constructor of, not {@code null}. Must be a concrete
     *          class.
     *
     * @param <T>
     *          The type of the class.
     *
     * @return
     *          The public no-arg constructor of the given type or {@code null} if no such constructor
     *          exists.
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> publicNoArgConstructor(Class<T> type) {
        var constructors = ReflectionUtils.findConstructors(type, constructor -> constructor.getParameterCount() == 0);

        if(constructors.isEmpty()) {
            log.warn("Class '{}' does not have a no-arg constructor!", type.getName());
            return null;
        }

        var noArgConstructor = constructors.get(0);
        var constructorModifiers = noArgConstructor.getModifiers();
        if(!Modifier.isPublic(constructorModifiers)) {
            log.warn("Class '{}' has a no-arg constructor, but it is not public!", type.getName());
            return null;
        }

        return (Constructor<T>) noArgConstructor;
    }

    /**
     *
     * @param type
     *          The type to check, not {@code null}. Must be a concrete class.
     *
     * @return
     *          {@code true}, if the given {@code type} has a public no-arg constructor available. {@code false}
     *          is returned otherwise.
     */
    private static boolean hasPublicNoArgConstructor(Class<?> type) {
        return publicNoArgConstructor(type) != null;
    }

    /**
     *
     * @param type
     *          The type to instantiate, not {@code null}. Must have a public no-arg constructor.
     *
     * @param <T>
     *          The type.
     *
     * @return
     *          An instance of the given {@code type} created by using the public no-arg constructor.
     *
     * @throws IllegalStateException
     *          When an error occurs while instantiating.
     */
    private static <T> T instantiateWithPublicNoArgConstructor(Class<T> type) {
        try {
            return publicNoArgConstructor(type).newInstance();
        } catch (Exception e) {
            log.error("Error instantiating '{}' with public no-arg constructor!", type.getName());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Initializes the {@link #reflections} field in case it is {@code null}.
     *
     * @return
     *          The initialized {@link Reflections} instance.
     */
    private static Reflections initReflections() {
        if(reflections == null) {
            reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(BASE_PACKAGE))
                    .addScanners(new SubTypesScanner())
            );
        }

        return reflections;
    }

}
