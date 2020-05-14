package de.adesso.objectfieldcoverage.core.analyzer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LombokEqualsMethodAnalyzerTest {

    private LombokEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokEqualsMethodAnalyzer();
    }

    @Test
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtType<?> typeMock,
                                                               @Mock Data dataMock) {
        // given
        given(typeMock.getAnnotation(Data.class)).willReturn(dataMock);

        // when
        var actualResult = testSubject.overridesEquals(typeMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtType<?> typeMock,
                                                               @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(typeMock.getAnnotation(Data.class)).willReturn(null);
        given(typeMock.getAnnotation(EqualsAndHashCode.class)).willReturn(equalsAndHashCodeMock);

        // when
        var actualResult = testSubject.overridesEquals(typeMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void overridesEqualsReturnsFalseWhenTypeIsNotAnnotatedWithDataOrEqualsAndHashCode(@Mock CtType<?> typeMock) {
        // given
        given(typeMock.getAnnotation(Data.class)).willReturn(null);
        given(typeMock.getAnnotation(EqualsAndHashCode.class)).willReturn(null);

        // when
        var actualResult = testSubject.overridesEquals(typeMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsTrueWhenCallsSuperFlagSet(@Mock CtMethod<Boolean> equalsMethodMock,
                                                    @Mock CtType declaringTypeMock,
                                                    @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(equalsMethodMock.getDeclaringType()).willReturn(declaringTypeMock);

        given(declaringTypeMock.getAnnotation(EqualsAndHashCode.class)).willReturn(equalsAndHashCodeMock);

        given(equalsAndHashCodeMock.callSuper()).willReturn(true);

        // when
        var actualResult = testSubject.callsSuper(equalsMethodMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsFalseWhenCallsSuperFlagNotSet(@Mock CtMethod<Boolean> equalsMethodMock,
                                                        @Mock CtType declaringTypeMock,
                                                        @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(equalsMethodMock.getDeclaringType()).willReturn(declaringTypeMock);

        given(declaringTypeMock.getAnnotation(EqualsAndHashCode.class)).willReturn(equalsAndHashCodeMock);

        given(equalsAndHashCodeMock.callSuper()).willReturn(false);

        // when
        var actualResult = testSubject.callsSuper(equalsMethodMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsFalseWhenDeclaringTypeNotAnnotated(@Mock CtMethod<Boolean> equalsMethodMock,
                                                             @Mock CtType declaringTypeMock) {
        // given
        given(equalsMethodMock.getDeclaringType()).willReturn(declaringTypeMock);

        given(declaringTypeMock.getAnnotation(EqualsAndHashCode.class)).willReturn(null);

        // when
        var actualResult = testSubject.callsSuper(equalsMethodMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
