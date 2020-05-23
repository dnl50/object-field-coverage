package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessibleFieldGraphTest {

    @Test
    void getAllNodesReturnsExpectedNodes(@Mock AccessibleFieldGraphNode firstRootNodeMock,
                                         @Mock AccessibleFieldGraphNode secondRootNodeMock,
                                         @Mock AccessibleFieldGraphNode childNodeMock,
                                         @Mock AccessibleFieldGraphNode grandChildMock) {
        // given
        var testSubject = new AccessibleFieldGraph(firstRootNodeMock, secondRootNodeMock);

        given(firstRootNodeMock.getChildren()).willReturn(Set.of(firstRootNodeMock, childNodeMock));
        given(secondRootNodeMock.getChildren()).willReturn(Set.of(childNodeMock));
        given(childNodeMock.getChildren()).willReturn(Set.of(grandChildMock));
        given(grandChildMock.getChildren()).willReturn(Set.of(firstRootNodeMock));

        // when
        var actualNodes = testSubject.getAllNodes();

        // then
        assertThat(actualNodes).containsExactlyInAnyOrder(firstRootNodeMock, secondRootNodeMock, childNodeMock,
                grandChildMock);
    }

}
