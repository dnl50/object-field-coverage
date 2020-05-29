package de.adesso.objectfieldcoverage.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtModifiable;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessModifierTest {

    @Test
    void ofReturnsPublicWhenModifiableIsPublic(@Mock CtModifiable modifiableMock) {
        // given
        given(modifiableMock.isPublic()).willReturn(true);

        // when
        var actualResult = AccessModifier.of(modifiableMock);

        // then
        assertThat(actualResult).isEqualTo(AccessModifier.PUBLIC);
    }

    @Test
    void ofReturnsProtectedWhenModifiableIsProtected(@Mock CtModifiable modifiableMock) {
        // given
        given(modifiableMock.isProtected()).willReturn(true);

        // when
        var actualResult = AccessModifier.of(modifiableMock);

        // then
        assertThat(actualResult).isEqualTo(AccessModifier.PROTECTED);
    }

    @Test
    void ofReturnsPackageWhenModifiableIsPackage(@Mock CtModifiable modifiableMock) {
        // given

        // when
        var actualResult = AccessModifier.of(modifiableMock);

        // then
        assertThat(actualResult).isEqualTo(AccessModifier.PACKAGE);
    }

    @Test
    void ofReturnsPrivateWhenModifiableIsPrivate(@Mock CtModifiable modifiableMock) {
        // given
        given(modifiableMock.isPrivate()).willReturn(true);

        // when
        var actualResult = AccessModifier.of(modifiableMock);

        // then
        assertThat(actualResult).isEqualTo(AccessModifier.PRIVATE);
    }

}
