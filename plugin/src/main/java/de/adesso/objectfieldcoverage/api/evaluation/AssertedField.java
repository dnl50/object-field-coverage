package de.adesso.objectfieldcoverage.api.evaluation;

import spoon.reflect.declaration.CtField;

import java.util.List;

public interface AssertedField<T> {

    CtField<T> getCtField();

    List<AssertedField<?>> getAccessibleChildFields();

    AssertedField<?> getParent();

    String getName();

}
