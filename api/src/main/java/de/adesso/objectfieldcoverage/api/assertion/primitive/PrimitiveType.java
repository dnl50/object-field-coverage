package de.adesso.objectfieldcoverage.api.assertion.primitive;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PrimitiveType {

    BOOLEAN("boolean"),

    BYTE("byte"),

    CHAR("char"),

    SHORT("short"),

    INT("int"),

    LONG("long"),

    FLOAT("float"),

    DOUBLE("double");

    private final String name;

}
