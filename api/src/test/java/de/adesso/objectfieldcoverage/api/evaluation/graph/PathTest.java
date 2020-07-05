package de.adesso.objectfieldcoverage.api.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PathTest {

    @Test
    void constructorThrowsExceptionWhenNodeListIsNotAPath(@Mock AccessibleFieldGraphNode startNode,
                                                          @Mock AccessibleFieldGraphNode secondNode,
                                                          @Mock AccessibleFieldGraphNode thirdNode) {
        // given
        // thirdNode is not a child of secondNode -> not a path
        given(secondNode.getChildren()).willReturn(Set.of());

        // when / then
        assertThatThrownBy(() -> new Path(List.of(startNode, secondNode, thirdNode)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given nodes list is not a valid path!");
    }

    @Test
    void getLastReturnsEmptyOptionalWhenPathIsEmpty() {
        // given
        var testSubject = new Path();

        // when
        var actualResult = testSubject.getLast();

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    void getLastReturnsPopulatedOptionalWhenPathIsNotEmpty(@Mock AccessibleFieldGraphNode startMock,
                                                           @Mock AccessibleFieldGraphNode endNodeMock) {
        // given
        given(startMock.getChildren()).willReturn(Set.of(endNodeMock));

        var testSubject = new Path(startMock, endNodeMock);

        // when
        var actualResult = testSubject.getLast();

        // then
        assertThat(actualResult).contains(endNodeMock);
    }

    @Test
    void toStringReturnsExpectedResultForEmptyPath() {
        // given
        var testSubject = new Path();
        var expectedResult = "Path(length=0, simpleNamesOfFieldsOnPath=[])";

        // when
        var actualResult = testSubject.toString();

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void isEmptyReturnsTrueWhenPathDoesNotContainNodes() {
        // given
        var testSubject = new Path();

        // when
        var actualResult = testSubject.isEmpty();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isEmptyReturnsTrueWhenPathDoesContainsNode(@Mock AccessibleFieldGraphNode nodeMock) {
        // given
        var testSubject = new Path(nodeMock);

        // when
        var actualResult = testSubject.isEmpty();

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void prependReturnsSamePathWhenPathIsEmpty() {
        // given
        var pathToPrepend = new Path();
        var testSubject = new Path();

        // when
        var actualPath = testSubject.prepend(pathToPrepend);

        // then
        assertThat(actualPath).isSameAs(testSubject);
    }

    @Test
    void prependReturnsSamePathWhenPathIsNull() {
        // given
        var testSubject = new Path();

        // when
        var actualPath = testSubject.prepend(null);

        // then
        assertThat(actualPath).isSameAs(testSubject);
    }

    @Test
    void prependReturnsOtherPathWhenPathIsEmpty(@Mock AccessibleFieldGraphNode nodeMock) {
        // given
        var pathToPrepend = new Path(nodeMock);
        var testSubject = new Path();

        // when
        var actualPath = testSubject.prepend(pathToPrepend);

        // then
        assertThat(actualPath).isSameAs(pathToPrepend);
    }

    @Test
    void prependReturnsNewPathWhenPrependPathIsValid(@Mock AccessibleFieldGraphNode prependNodeMock,
                                                     @Mock AccessibleFieldGraphNode nodeMock) {
        // given
        given(prependNodeMock.getChildren()).willReturn(Set.of(nodeMock));

        var pathToPrepend = new Path(prependNodeMock);
        var expectedPath = new Path(prependNodeMock, nodeMock);

        var testSubject = new Path(nodeMock);

        // when
        var actualPath = testSubject.prepend(pathToPrepend);

        // then
        assertThat(actualPath).isEqualTo(expectedPath);
    }

    @Test
    void prependThrowsExceptionWhenLastNodeOfPrependPathIsNotAParent(@Mock AccessibleFieldGraphNode prependNodeMock,
                                                                     @Mock AccessibleFieldGraphNode nodeMock) {
        // given
        var pathToPrepend = new Path(prependNodeMock);

        var testSubject = new Path(nodeMock);

        given(prependNodeMock.getChildren()).willReturn(Set.of());

        // when / then
        assertThatThrownBy(() -> testSubject.prepend(pathToPrepend))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The last node of the given path is not a parent node of the first node of this path!");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void toStringReturnsExpectedResultForPathOfLengthOne(@Mock AccessibleFieldGraphNode nodeMock,
                                                         @Mock AccessibleField accessibleFieldMock,
                                                         @Mock CtField fieldMock) {
        // given
        var givenFieldSimpleName = "field";
        var expectedResult = String.format("Path(length=1, simpleNamesOfFieldsOnPath=[%s])", givenFieldSimpleName);
        var testSubject = new Path(nodeMock);

        given(nodeMock.getAccessibleField()).willReturn(accessibleFieldMock);
        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(fieldMock.getSimpleName()).willReturn(givenFieldSimpleName);

        // when
        var actualResult = testSubject.toString();

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void toStringReturnsExpectedResultForPathOfLengthTwo(@Mock AccessibleFieldGraphNode nodeMock,
                                                         @Mock AccessibleField accessibleFieldMock,
                                                         @Mock CtField fieldMock) {
        // given
        var firstFieldSimpleName = "field";
        var secondFieldSimpleName = "otherField";
        var expectedResult = String.format("Path(length=2, simpleNamesOfFieldsOnPath=[%s->%s])", firstFieldSimpleName,
                secondFieldSimpleName);

        given(nodeMock.getChildren()).willReturn(Set.of(nodeMock));
        given(nodeMock.getAccessibleField()).willReturn(accessibleFieldMock);
        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(fieldMock.getSimpleName()).willReturn(firstFieldSimpleName, secondFieldSimpleName);

        var testSubject = new Path(nodeMock, nodeMock);

        // when
        var actualResult = testSubject.toString();

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void containsLoopReturnsTrueWhenSameNodeAsNextElement(@Mock AccessibleFieldGraphNode nodeMock) {
        // given
        given(nodeMock.getChildren()).willReturn(Set.of(nodeMock));

        var testSubject = new Path(List.of(nodeMock, nodeMock));

        // when
        var actualResult = testSubject.containsLoop();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void containsLoopReturnsTrueWhenSameNodeAppearsLater(@Mock AccessibleFieldGraphNode nodeMock,
                                                         @Mock AccessibleFieldGraphNode otherNodeMock) {
        // given
        given(otherNodeMock.getChildren()).willReturn(Set.of(nodeMock));
        given(nodeMock.getChildren()).willReturn(Set.of(otherNodeMock));

        var testSubject = new Path(List.of(nodeMock, otherNodeMock, nodeMock));

        // when
        var actualResult = testSubject.containsLoop();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void containsLoopReturnsFalseWhenPathIsEmpty() {
        // given
        var testSubject = new Path();

        // when
        var actualResult = testSubject.containsLoop();

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void containsLoopReturnsFalseWhenDoesNotContainLoop(@Mock AccessibleFieldGraphNode nodeMock,
                                                        @Mock AccessibleFieldGraphNode otherNodeMock) {
        // given
        given(nodeMock.getChildren()).willReturn(Set.of(otherNodeMock));

        var testSubject = new Path(List.of(nodeMock, otherNodeMock));

        // when
        var actualResult = testSubject.containsLoop();

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void appendAppendsChildNodeWhenPathIsEmpty(@Mock AccessibleFieldGraphNode nodeToAppendMock) {
        // given
        var testSubject = new Path();
        var expectedPath = new Path(nodeToAppendMock);

        // when
        var actualPath = testSubject.append(nodeToAppendMock);

        // then
        assertThat(actualPath).isEqualTo(expectedPath);
    }

    @Test
    void appendAppendsChildNodeWhenLastNodeIsParentNode(@Mock AccessibleFieldGraphNode lastNodeMock,
                                                        @Mock AccessibleFieldGraphNode nodeToAppendMock) {
        // given
        given(lastNodeMock.getChildren()).willReturn(Set.of(nodeToAppendMock));

        var testSubject = new Path(lastNodeMock);
        var expectedPath = new Path(lastNodeMock, nodeToAppendMock);

        // when
        var actualPath = testSubject.append(nodeToAppendMock);

        // then
        assertThat(actualPath).isEqualTo(expectedPath);
    }

    @Test
    void appendThrowsExceptionWhenNewNodeIsNotAChildNodeOfLastNode(@Mock AccessibleFieldGraphNode lastNodeMock,
                                                                   @Mock AccessibleFieldGraphNode nodeToAppendMock) {
        // given
        given(lastNodeMock.getChildren()).willReturn(Set.of());

        var testSubject = new Path(lastNodeMock);

        // when / then
        assertThatThrownBy(() -> testSubject.append(nodeToAppendMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given node is not a child node of the current last node!");
    }

    @Test
    void startsWithReturnsTrueWhenSamePathObject(@Mock AccessibleFieldGraphNode firstNodeMock,
                                                 @Mock AccessibleFieldGraphNode secondNodeMock) {
        // given
        given(firstNodeMock.getChildren()).willReturn(Set.of(secondNodeMock));

        var testSubject = new Path(firstNodeMock, secondNodeMock);

        // when
        var actualResult = testSubject.startsWith(testSubject);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void startsWithReturnsTrueWhenOtherPathIsSubPath(@Mock AccessibleFieldGraphNode firstNodeMock,
                                                     @Mock AccessibleFieldGraphNode secondNodeMock) {
        // given
        given(firstNodeMock.getChildren()).willReturn(Set.of(secondNodeMock));

        var testSubject = new Path(firstNodeMock, secondNodeMock);
        var otherPath = new Path(firstNodeMock);

        // when
        var actualResult = testSubject.startsWith(otherPath);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void startsWithReturnsTrueWhenSamePath(@Mock AccessibleFieldGraphNode firstNodeMock,
                                           @Mock AccessibleFieldGraphNode secondNodeMock) {
        // given
        given(firstNodeMock.getChildren()).willReturn(Set.of(secondNodeMock));

        var testSubject = new Path(firstNodeMock, secondNodeMock);
        var otherPath = new Path(firstNodeMock, secondNodeMock);

        // when
        var actualResult = testSubject.startsWith(otherPath);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void startsWithReturnsFalseWhenPathDoesNotStartWithOtherPath(@Mock AccessibleFieldGraphNode nodeMock,
                                                                 @Mock AccessibleFieldGraphNode otherNodeMock) {
        // given
        var testSubject = new Path(nodeMock);
        var otherPath = new Path(otherNodeMock);

        // when
        var actualResult = testSubject.startsWith(otherPath);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void startsWithReturnsFalseWhenOtherPathIsLonger(@Mock Path otherPathMock) {
        // given
        var testSubject = new Path();

        given(otherPathMock.getLength()).willReturn(1);

        // when
        var actualResult = testSubject.startsWith(otherPathMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
