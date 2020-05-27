package de.adesso.test;

import java.util.Objects;

public class ClassComparingAllFields {

    private String first;

    private String second;

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleClassComparingAllFields that = (SimpleClassComparingAllFields) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(getSecond(), that.getSecond());
    }

}
