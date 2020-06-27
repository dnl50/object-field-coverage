package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.assertion.reference.ThrowableAssertion;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ThrowablePseudoFieldFinderTest {

    @Mock
    private PseudoClassGenerator pseudoClassGeneratorMock;

    @Mock
    private PseudoFieldGenerator pseudoFieldGeneratorMock;

    private ThrowablePseudoFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new ThrowablePseudoFieldFinder(pseudoClassGeneratorMock, pseudoFieldGeneratorMock);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Throwable.class, Exception.class, RuntimeException.class, NullPointerException.class,
            IOException.class, Error.class
    })
    void containsPseudoFieldsReturnsTrueWhenClassExtendsThrowable(Class<? extends Throwable> throwableClass) {
        // given
        CtTypeReference<?> givenTypeRef = new TypeFactory().createReference(throwableClass);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            String.class, Integer.class
    })
    void containsPseudoFieldsReturnsTrueWhenClassDoesNotExtendThrowable(Class<? extends Throwable> throwableClass) {
        // given
        CtTypeReference<?> givenTypeRef = new TypeFactory().createReference(throwableClass);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void fieldNamesAndTypesReturnsExpectedPairs() {
        // given
        var booleanPrimTypeRef = new TypeFactory().BOOLEAN_PRIMITIVE;
        Set<Pair<String, CtTypeReference<?>>> expectedPairs = Set.of(
                Pair.of(ThrowableAssertion.CAUSE_PSEUDO_FIELD_NAME, booleanPrimTypeRef),
                Pair.of(ThrowableAssertion.MESSAGE_PSEUDO_FIELD_NAME, booleanPrimTypeRef),
                Pair.of(ThrowableAssertion.TYPE_PSEUDO_FIELD_NAME, booleanPrimTypeRef)
        );

        // when
        var actualPairs = testSubject.fieldNamesAndTypes(null);

        // then
        assertThat(actualPairs).containsExactlyInAnyOrderElementsOf(expectedPairs);
    }

}
