package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessibleFieldGraphTest {

    @Test
    void getAllNodesReturnsExpectedNodes(@Mock AccessibleFieldGraphNode firstRootNodeMock,
                                         @Mock AccessibleFieldGraphNode secondRootNodeMock,
                                         @Mock AccessibleFieldGraphNode childNodeMock,
                                         @Mock AccessibleFieldGraphNode grandChildMock,
                                         @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, firstRootNodeMock, secondRootNodeMock);

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

    @Test
    void getTransitiveReachabilityPathsReturnsExpectedPaths(@Mock AccessibleFieldGraphNode firstRootNodeMock,
                                                            @Mock AccessibleFieldGraphNode secondRootNodeMock,
                                                            @Mock AccessibleFieldGraphNode childNodeMock,
                                                            @Mock AccessibleFieldGraphNode grandChildMock,
                                                            @Mock AccessibleFieldGraphNode otherGrandChildMock,
                                                            @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, firstRootNodeMock, secondRootNodeMock);

        given(firstRootNodeMock.getChildren()).willReturn(Set.of(firstRootNodeMock, childNodeMock));
        given(secondRootNodeMock.getChildren()).willReturn(Set.of(childNodeMock));
        given(childNodeMock.getChildren()).willReturn(Set.of(grandChildMock, otherGrandChildMock));
        given(grandChildMock.getChildren()).willReturn(Set.of(firstRootNodeMock));
        given(otherGrandChildMock.getChildren()).willReturn(Set.of());

        var expectedPaths = Set.of(
                new Path(firstRootNodeMock, firstRootNodeMock),
                new Path(firstRootNodeMock, childNodeMock, grandChildMock, firstRootNodeMock),
                new Path(firstRootNodeMock, childNodeMock, otherGrandChildMock),
                new Path(secondRootNodeMock, childNodeMock, grandChildMock, firstRootNodeMock, firstRootNodeMock),
                new Path(secondRootNodeMock, childNodeMock, grandChildMock, firstRootNodeMock, childNodeMock),
                new Path(secondRootNodeMock, childNodeMock, otherGrandChildMock)
        );

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactlyInAnyOrderElementsOf(expectedPaths);
    }

}
