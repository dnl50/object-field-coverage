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

}
