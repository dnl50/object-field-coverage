package de.adesso.test;

import java.util.Objects;

public class ClassOmittingSingleFieldFromEquals {

    private String included;

    private String notIncluded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassOmittingSingleFieldFromEquals that = (ClassOmittingSingleFieldFromEquals) o;
        return Objects.equals(included, that.included);
    }

}
