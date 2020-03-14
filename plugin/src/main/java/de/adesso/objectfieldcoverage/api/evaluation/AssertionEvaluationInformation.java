package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtType;

import java.util.List;

@Data
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    private final CtType<?> assertedType;

    private final List<AssertedField<?>> accessibleFields;

    private final List<AssertedField<?>> accessibleFieldsUsedInEquals;

}
