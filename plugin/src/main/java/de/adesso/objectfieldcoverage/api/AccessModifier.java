package de.adesso.objectfieldcoverage.api;

import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtModifiable;

import java.util.Objects;

/**
 * Enum representation of the class, interface, field, method and constructor access modifiers
 * specified in the Java Language Specification (Java SE 11 Edition). Constants are ordered
 * in descending order by their accessibility strictness ({@code private} &rarr; {@code package},
 * {@code protected} &rarr; {@code public}).
 */
@RequiredArgsConstructor
public enum AccessModifier {

    /**
     * <i>private</i> access modifier.
     */
    PRIVATE,

    /**
     * <i>package</i> access modifier.
     */
    PACKAGE,

    /**
     * <i>protected</i> access modifier.
     */
    PROTECTED,

    /**
     * <i>public</i> access modifier.
     */
    PUBLIC;

    /**
     *
     * @param modifiable
     *          The {@link CtModifiable} to get the {@link AccessModifier} for, not {@code null}.
     *
     * @return
     *          The specified access modifier of the given {@code modifiable}.
     */
    public static AccessModifier of(CtModifiable modifiable) {
        Objects.requireNonNull(modifiable, "The given CtModifiable cannot be null!");

        if(modifiable.isPublic()) {
            return PUBLIC;
        } else if(modifiable.isProtected()) {
            return PROTECTED;
        } else if(modifiable.isPrivate()) {
            return PRIVATE;
        }

        return PACKAGE;
    }

}
