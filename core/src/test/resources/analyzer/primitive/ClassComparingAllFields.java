package de.adesso.test;

import java.util.Objects;

public class ClassComparingAllFields {

    private char charField;

    private byte byteField;

    private short shortField;

    private int intField;

    private long longField;

    private boolean booleanField;

    private float floatField;

    private double doubleField;

    private String stringField;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassComparingAllFields that = (ClassComparingAllFields) o;
        return charField == that.charField &&
                byteField == that.byteField &&
                shortField == that.shortField &&
                intField == that.intField &&
                longField == that.longField &&
                booleanField == that.booleanField &&
                that.floatField == floatField &&
                that.doubleField == doubleField &&
                stringField == that.stringField;
    }

}
