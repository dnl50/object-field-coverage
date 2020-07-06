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
    void getTransitiveReachabilityPathsReturnsEmptyPathWhenNoRootNodes(@Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock);

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactly(new Path());
    }

    @Test
    void getTransitiveReachabilityPathsReturnsPathsForRootNodesWhenNoChildNodes(@Mock AccessibleFieldGraphNode rootNodeMock,
                                                                                @Mock AccessibleFieldGraphNode otherRootNodeMock,
                                                                                @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, rootNodeMock, otherRootNodeMock);

        given(rootNodeMock.getChildren()).willReturn(Set.of());
        given(otherRootNodeMock.getChildren()).willReturn(Set.of());

        var expectedPaths = Set.of(
                new Path(rootNodeMock),
                new Path(otherRootNodeMock)
        );

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactlyInAnyOrderElementsOf(expectedPaths);
    }

    @Test
    void getTransitiveReachabilityPathsReturnsPathsEndingWithSingleCycle(@Mock AccessibleFieldGraphNode rootNodeMock,
                                                                         @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, rootNodeMock);

        given(rootNodeMock.getChildren()).willReturn(Set.of(rootNodeMock));

        var expectedPath = new Path(rootNodeMock, rootNodeMock);

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactly(expectedPath);
    }

    @Test
    void getTransitiveReachabilityPathsReturnsPathsEndingWithSingleCycleOverMultipleNodes(@Mock AccessibleFieldGraphNode rootNodeMock,
                                                                                          @Mock AccessibleFieldGraphNode otherRootNodeMock,
                                                                                          @Mock AccessibleFieldGraphNode childNode,
                                                                                          @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, rootNodeMock);

        given(rootNodeMock.getChildren()).willReturn(Set.of(childNode));
        given(childNode.getChildren()).willReturn(Set.of(otherRootNodeMock));
        given(otherRootNodeMock.getChildren()).willReturn(Set.of(childNode));

        var expectedPath = new Path(rootNodeMock, childNode, otherRootNodeMock, childNode);

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactly(expectedPath);
    }

    @Test
    void getTransitiveReachabilityPathsReturnsPathForEachChildNode(@Mock AccessibleFieldGraphNode rootNodeMock,
                                                                   @Mock AccessibleFieldGraphNode childNode,
                                                                   @Mock AccessibleFieldGraphNode otherChildNode,
                                                                   @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, rootNodeMock);

        given(rootNodeMock.getChildren()).willReturn(Set.of(childNode, otherChildNode));
        given(childNode.getChildren()).willReturn(Set.of());
        given(otherChildNode.getChildren()).willReturn(Set.of());

        var expectedPaths = Set.of(
                new Path(rootNodeMock, childNode),
                new Path(rootNodeMock, otherChildNode)
        );

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactlyInAnyOrderElementsOf(expectedPaths);
    }

    @Test
    void getTransitiveReachabilityPathsReturnsPathsEndingWithLeafs(@Mock AccessibleFieldGraphNode rootNodeMock,
                                                                   @Mock AccessibleFieldGraphNode leafMock,
                                                                   @Mock CtTypeReference<?> typeRefMock) {
        // given
        var testSubject = new AccessibleFieldGraph(typeRefMock, typeRefMock, rootNodeMock);

        given(rootNodeMock.getChildren()).willReturn(Set.of(leafMock));
        given(leafMock.getChildren()).willReturn(Set.of());

        var expectedPath = new Path(rootNodeMock, leafMock);

        // when
        var actualPaths = testSubject.getTransitiveReachabilityPaths();

        // then
        assertThat(actualPaths).containsExactly(expectedPath);
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
