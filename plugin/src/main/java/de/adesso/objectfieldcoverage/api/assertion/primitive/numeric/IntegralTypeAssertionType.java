package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IntegralTypeAssertionType implements PrimitiveType {
    BYTE("byte", Byte.MIN_VALUE, Byte.MAX_VALUE),
    SHORT("short", Short.MIN_VALUE, Short.MAX_VALUE),
    INT("int", Integer.MIN_VALUE, Integer.MAX_VALUE),
    LONG("long", Long.MIN_VALUE, Long.MAX_VALUE),
    CHAR("char", Character.MIN_VALUE, Character.MAX_VALUE);

    private final String typeName;

    @Getter
    private final long minValue;

    @Getter
    private final long maxValue;

    @Override
    public String getPrimitiveTypeName() {
        return typeName;
    }

}
