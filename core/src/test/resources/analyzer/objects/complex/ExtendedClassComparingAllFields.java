package de.adesso.test;

import java.util.Objects;

public class ExtendedClassComparingAllFields extends ClassComparingAllFields {

    private String third;

    private String fourth;

    public String getThird() {
        return first;
    }

    public String getFourth() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedClassComparingAllFields that = (ExtendedClassComparingAllFields) o;
        return Objects.equals(third, that.third) &&
                Objects.equals(fourth, that.fourth) &&
                Objects.equals(getFirst(), that.getFirst()) &&
                Objects.equals(getSecond(), that.getSecond());
    }

}
