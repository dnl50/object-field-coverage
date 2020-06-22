package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathUtilsTest {

    @Test
    void loadClassesImplementingInterfaceOrExtendingClassLoadsExpectedClassesRespectingOrder() {
        // given
        var expectedClassInstances = List.<Class<?>>of(
                ConcreteClassImplementingExtendingInterfaceHighestPriority.class,
                ConcreteClassImplementingExtendingInterface.class,
                ConcreteClassImplementingBaseInterface.class
        );

        // when
        var actualInstances = ClasspathUtils.loadClassesImplementingInterfaceOrExtendingClass(BaseInterface.class);

        // then
        assertThat(actualInstances.stream().map(Object::getClass).collect(Collectors.<Class<?>>toList()))
                .containsExactlyElementsOf(expectedClassInstances);
    }

}

interface BaseInterface {

}

interface ExtendingInterface extends BaseInterface {

}

abstract class AbstractClassImplementingExtendingInterface implements ExtendingInterface {

    public AbstractClassImplementingExtendingInterface() {

    }

}

class ConcreteClassImplementingBaseInterfaceWithoutPublicConstructor implements BaseInterface {

}

@Order(Order.LOWEST)
class ConcreteClassImplementingBaseInterface implements BaseInterface {

    public ConcreteClassImplementingBaseInterface() {

    }

}

class ConcreteClassImplementingExtendingInterface implements ExtendingInterface {

    public ConcreteClassImplementingExtendingInterface() {

    }

}

@Order(Order.HIGHEST)
class ConcreteClassImplementingExtendingInterfaceHighestPriority implements ExtendingInterface {

    public ConcreteClassImplementingExtendingInterfaceHighestPriority() {

    }

}
