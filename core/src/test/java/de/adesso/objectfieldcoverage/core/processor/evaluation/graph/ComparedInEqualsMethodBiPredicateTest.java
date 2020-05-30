package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ComparedInEqualsMethodBiPredicateTest {

    @Mock
    private EqualsMethodAnalyzer equalsMethodAnalyzerMock;

    @Mock
    private AccessibilityAwareFieldFinder fieldFinderMock;

    private ComparedInEqualsMethodBiPredicate testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new ComparedInEqualsMethodBiPredicate(List.of(equalsMethodAnalyzerMock), List.of(fieldFinderMock));
    }

    @Test
    void testReturnsFalseWhenGivenTypeIsNotAClass(@Mock AccessibleField<String> accessibleFieldMock,
                                                  @Mock CtType<String> typeMock) {
        // given

        // when
        var actualResult = testSubject.test(accessibleFieldMock, typeMock);

        // then
        assertThat(actualResult).isFalse();
    }

    //TODO: add additional tests

}
