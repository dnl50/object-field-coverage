package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.annotation.CalculateCoverage;
import de.adesso.objectfieldcoverage.annotation.TestTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnnotationUtilsTest {

    @Test
    void isAnnotatedWithReturnsTrueWhenElementIsAnnotated(@Mock CtElement elementMock,
                                                          @Mock TestTarget testTargetMock) {
        // given
        var givenAnnotationType = TestTarget.class;

        given(elementMock.getAnnotation(givenAnnotationType)).willReturn(testTargetMock);

        // when
        var actualResult = AnnotationUtils.isAnnotatedWith(elementMock, givenAnnotationType);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isAnnotatedWithReturnsFalseWhenElementIsNotAnnotated(@Mock CtElement elementMock) {
        // given
        var givenAnnotationType = TestTarget.class;

        given(elementMock.getAnnotation(givenAnnotationType)).willReturn(null);

        // when
        var actualResult = AnnotationUtils.isAnnotatedWith(elementMock, givenAnnotationType);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void childElementAnnotatedWithReturnsTrueWhenAnyChildAnnotated(@Mock CtElement element,
                                                                   @Mock CtElement childElementMock,
                                                                   @Mock CtElement annotatedChildElementMock,
                                                                   @Mock CalculateCoverage calculateCoverageMock) {
        // given
        given(element.getElements(any(TypeFilter.class))).willReturn(List.of(childElementMock, annotatedChildElementMock));

        given(annotatedChildElementMock.getAnnotation(CalculateCoverage.class)).willReturn(calculateCoverageMock);

        // when
        var actualResult = AnnotationUtils.childElementAnnotatedWith(element, CtElement.class, CalculateCoverage.class);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void childElementAnnotatedWithReturnsFalseWhenNoChildAnnotated(@Mock CtElement element,
                                                                   @Mock CtElement childElementMock) {
        // given
        given(element.getElements(any(TypeFilter.class))).willReturn(List.of(childElementMock));


        // when
        var actualResult = AnnotationUtils.childElementAnnotatedWith(element, CtElement.class, CalculateCoverage.class);

        // then
        assertThat(actualResult).isFalse();
    }

}
