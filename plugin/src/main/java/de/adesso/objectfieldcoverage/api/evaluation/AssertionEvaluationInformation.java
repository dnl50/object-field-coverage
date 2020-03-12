package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtType;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    private final CtType<?> assertedType;

    private final List<AssertedField<?>> accessibleFields;

    private final List<AssertedField<?>> accessibleFieldsUsedInEquals;

    public List<AssertedField<?>> getAccessibleFields() {
        return Collections.unmodifiableList(accessibleFields);
    }

    public List<AssertedField<?>> getAccessibleFieldsUsedInEquals() {
        return Collections.unmodifiableList(accessibleFieldsUsedInEquals);
    }

}
