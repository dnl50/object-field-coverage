package de.adesso.objectfieldcoverage.core.junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JUnit4TestMethodFinderTest {

    private JUnit4TestMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JUnit4TestMethodFinder();
    }

    @Test
    void findTestMethodsReturnsPublicTestMethods(@Mock CtClass<?> testClazzMock,
                                                 @Mock CtMethod<?> nonPublicTestMethodMock,
                                                 @Mock CtMethod<?> publicMethodMock,
                                                 @Mock CtMethod<?> publicTestMethodMock,
                                                 @Mock org.junit.Test testAnnotationMock) {
        // given
        given(testClazzMock.getAllMethods()).willReturn(Set.of(nonPublicTestMethodMock, publicMethodMock, publicTestMethodMock));

        given(nonPublicTestMethodMock.getAnnotation(org.junit.Test.class)).willReturn(testAnnotationMock);
        given(nonPublicTestMethodMock.isPublic()).willReturn(false);

        given(publicMethodMock.getAnnotation(org.junit.Test.class)).willReturn(null);

        given(publicTestMethodMock.getAnnotation(org.junit.Test.class)).willReturn(testAnnotationMock);
        given(publicTestMethodMock.isPublic()).willReturn(true);

        // when
        var actualMethods = testSubject.findTestMethods(testClazzMock);

        // then
        assertThat(actualMethods).containsExactly(publicTestMethodMock);
    }

}
