package de.adesso.objectfieldcoverage.core.junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractJUnitTestMethodFinderTest {

    private AbstractJUnitTestMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DefaultAbstractJUnitTestMethodFinder();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findTestMethodsReturnsAllMethodsOfTestClazzAndNonAbstractStaticInnerClasses(@Mock CtClass<?> testClazzMock,
                                                                                     @Mock CtClass<?> innerClassMock,
                                                                                     @Mock CtMethod<?> testClazzMethod,
                                                                                     @Mock CtMethod<?> innerClassMethod) {
        // given
        given(testClazzMock.getElements(any(TypeFilter.class))).willReturn(List.of(innerClassMock));
        given(testClazzMock.getAllMethods()).willReturn(Set.of(testClazzMethod));

        given(innerClassMock.isStatic()).willReturn(true);
        given(innerClassMock.getAllMethods()).willReturn(Set.of(innerClassMethod));


        // when
        var actualTestMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualTestMethods).containsExactlyInAnyOrder(testClazzMethod, innerClassMethod);
    }

    @Test
    void findTestMethodsReturnsEmptyListWhenTestClazzIsAbstract(@Mock CtClass<?> testClazzMock) {
        // given
        given(testClazzMock.isAbstract()).willReturn(true);

        // when
        var actualTestMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualTestMethods).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findTestMethodsReturnsIgnoresMethodsOfAbstractInnerClasses(@Mock CtClass<?> testClazzMock,
                                                                    @Mock CtClass<?> abstractInnerClassMock) {
        // given
        given(testClazzMock.getElements(any(TypeFilter.class))).willReturn(List.of(abstractInnerClassMock));
        given(testClazzMock.getAllMethods()).willReturn(Set.of());

        given(abstractInnerClassMock.isAbstract()).willReturn(true);

        // when
        var actualTestMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualTestMethods).isEmpty();

        verify(abstractInnerClassMock, never()).getAllMethods();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findTestMethodsReturnsIgnoresMethodsOfNonStaticInnerClasses(@Mock CtClass<?> testClazzMock,
                                                                     @Mock CtClass<?> nonStaticInnerClassMock) {
        // given
        given(testClazzMock.getElements(any(TypeFilter.class))).willReturn(List.of(nonStaticInnerClassMock));
        given(testClazzMock.getAllMethods()).willReturn(Set.of());

        given(nonStaticInnerClassMock.isStatic()).willReturn(false);

        // when
        var actualTestMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualTestMethods).isEmpty();

        verify(nonStaticInnerClassMock, never()).getAllMethods();
    }

    @Test
    void findTestMethodsThrowsExceptionWhenTestClazzIsNull() {
        // given / when / then
        assertThatThrownBy(() -> testSubject.findTestMethods(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("testClazz cannot be null!");
    }

    private static class DefaultAbstractJUnitTestMethodFinder extends AbstractJUnitTestMethodFinder {

        @Override
        protected Predicate<CtMethod<?>> testMethodPredicate() {
            return ctMethod -> true;
        }

    }

}
