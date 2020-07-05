package de.adesso.objectfieldcoverage.api.assertion.primitive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;

/**
 * Enum containing an entry for each of the 8 primitive types.
 */
@Getter
@RequiredArgsConstructor
public enum PrimitiveType {

    BOOLEAN("boolean", true),

    BYTE("byte", false),

    CHAR("char", false),

    SHORT("short", false),

    INT("int", false),

    LONG("long", false),

    FLOAT("float", false),

    DOUBLE("double", false);

    /**
     * The simple name of the primitive type.
     */
    private final String simpleName;

    /**
     * A boolean flag indicating whether reference equality comparisons results in covered paths.
     */
    private final boolean referenceEqualitySupported;

    /**
     *
     * @param simpleName
     *          The simple name to find a {@link PrimitiveType} enum constant for.
     *
     * @return
     *          The {@link PrimitiveType} enum constant with the corresponding name, not {@code null}.
     *
     * @throws IllegalArgumentException
     *          When no {@link PrimitiveType} enum constant with the given {@code simpleName} was found.
     */
    public static PrimitiveType of(String simpleName) {
        return EnumSet.allOf(PrimitiveType.class).stream()
            .filter(primitiveType -> primitiveType.getSimpleName().equals(simpleName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("No PrimitiveType constant with name '%s' found!", simpleName)));
    }

}
