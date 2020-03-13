package de.adesso.objectfieldcoverage.core.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TargetMethodFinderTest {

    @Mock
    private CtModel modelMock;

    private TargetMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TargetMethodFinder();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodIdentifierIsValidButClassDoesNotExist() {
        // given
        var givenMethodIdentifier = "org.test.Test#test()";

        given(modelMock.getElements(any())).willReturn(List.of());

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodIdentifierIsValidButMethodDoesNotExist(@Mock CtClass<?> targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(null);

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameIsMissing() {
        // given
        var givenMethodIdentifier = "#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "Te st#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "test.Test#te st()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenPackageNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "0test.Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.0Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.Test#0test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleDotsFollowEachOther() {
        // given
        var givenMethodIdentifier = "test..Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenNoRhombPresent() {
        // given
        var givenMethodIdentifier = "test.Test test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleRhombsPresent() {
        // given
        var givenMethodIdentifier = "test.Test#Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListBracketsAreMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListBracketArePresentMultipleTimes() {
        // given
        var givenMethodIdentifier = "test.Test#test(())";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensionOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensionClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleCommaFollowEachOther() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,,java.lang.String)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListEndsWithComma() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensioBracketsAreNested() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[[]])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    private void assertIllegalArgumentExceptionIsThrown(String givenMethodIdentifier) {
        // when / then
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given method identifier '%s' is not a valid identifier!", givenMethodIdentifier);
    }

}
