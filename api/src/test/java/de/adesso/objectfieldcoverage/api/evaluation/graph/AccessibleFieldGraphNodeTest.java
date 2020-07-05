package de.adesso.objectfieldcoverage.api.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class})
class AccessibleFieldGraphNodeTest {

    @Test
    void isPseudoFieldNodeReturnsTrueWhenAccessibleFieldIsPseudoField(@Mock AccessibleField<String> pseudoFieldMock) {
        // given
        given(pseudoFieldMock.isPseudo()).willReturn(true);

        var testSubject = new AccessibleFieldGraphNode(pseudoFieldMock, Set.of());

        // when
        var actualResult = testSubject.isPseudoFieldNode();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPseudoFieldNodeReturnsFalseWhenAccessibleFieldIsNotAPseudoField(@Mock AccessibleField<String> pseudoFieldMock) {
        // given
        given(pseudoFieldMock.isPseudo()).willReturn(false);

        var testSubject = new AccessibleFieldGraphNode(pseudoFieldMock, Set.of());

        // when
        var actualResult = testSubject.isPseudoFieldNode();

        // then
        assertThat(actualResult).isFalse();
    }

}
