package de.adesso.objectfieldcoverage.core.junit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JUnitJupiterTestMethodFinderTest {

    private JUnitJupiterTestMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JUnitJupiterTestMethodFinder();
    }

    @Test
    void findTestMethodsReturnsNonPrivateTestMethods(@Mock CtClass<?> testClazzMock,
                                                     @Mock CtMethod<?> privateTestMethodMock,
                                                     @Mock CtMethod<?> publicMethodMock,
                                                     @Mock CtMethod<?> nonPrivateTestMethodMock,
                                                     @Mock CtMethod<?> nonPrivateRepeatedTestMethodMock,
                                                     @Mock CtMethod<?> nonPrivateParameterizedTestMethodMock,
                                                     @Mock CtMethod<?> nonPrivateTestFactoryMethodMock,
                                                     @Mock CtMethod<?> nonPrivateTestTemplateMethodMock) {
        // given
        given(testClazzMock.getAllMethods()).willReturn(Set.of(privateTestMethodMock, publicMethodMock,
                nonPrivateTestMethodMock, nonPrivateRepeatedTestMethodMock, nonPrivateParameterizedTestMethodMock,
                nonPrivateTestFactoryMethodMock, nonPrivateTestTemplateMethodMock));

        setUpMockToReturnAnnotation(privateTestMethodMock, Test.class);
        given(privateTestMethodMock.isPrivate()).willReturn(true);

        setUpNonPrivateMockToReturnAnnotation(nonPrivateTestMethodMock, Test.class);

        setUpNonPrivateMockToReturnAnnotation(nonPrivateRepeatedTestMethodMock, RepeatedTest.class);

        setUpNonPrivateMockToReturnAnnotation(nonPrivateParameterizedTestMethodMock, ParameterizedTest.class);

        setUpNonPrivateMockToReturnAnnotation(nonPrivateTestFactoryMethodMock, TestFactory.class);

        setUpNonPrivateMockToReturnAnnotation(nonPrivateTestTemplateMethodMock, TestTemplate.class);

        // when
        var actualMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualMethods).containsExactlyInAnyOrder(nonPrivateTestMethodMock, nonPrivateRepeatedTestMethodMock,
                nonPrivateParameterizedTestMethodMock, nonPrivateTestFactoryMethodMock, nonPrivateTestTemplateMethodMock);
    }


    private void setUpNonPrivateMockToReturnAnnotation(CtMethod<?> methodMock, Class<? extends Annotation> annotationType) {
        setUpMockToReturnAnnotation(methodMock, annotationType);
        given(methodMock.isPrivate()).willReturn(false);
    }

    @SuppressWarnings("unchecked")
    private void setUpMockToReturnAnnotation(CtMethod<?> methodMock, Class<? extends Annotation> annotationType) {
        given(methodMock.getAnnotation(any(Class.class))).willAnswer(invocation -> {
            var actualAnnotation = (Class<? extends Annotation>) invocation.getArgument(0);

            if(annotationType.equals(actualAnnotation)) {
                return mock(annotationType);
            }

            return null;
        });
    }

}
