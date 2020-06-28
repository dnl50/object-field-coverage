package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.annotation.TestTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtElement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AnnotationUtilsTest {

    @Test
    void isAnnotatedWithReturnsTrueWhenElementIsAnnotated(@Mock CtElement element,
                                                          @Mock TestTarget testTargetMock) {
        // given
        var givenAnnotationType = TestTarget.class;

        given(element.getAnnotation(givenAnnotationType)).willReturn(testTargetMock);

        // when
        var actualResult = AnnotationUtils.isAnnotatedWith(element, givenAnnotationType);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isAnnotatedWithReturnsFalseWhenElementIsNotAnnotated(@Mock CtElement element) {
        // given
        var givenAnnotationType = TestTarget.class;

        given(element.getAnnotation(givenAnnotationType)).willReturn(null);

        // when
        var actualResult = AnnotationUtils.isAnnotatedWith(element, givenAnnotationType);

        // then
        assertThat(actualResult).isFalse();
    }

}
