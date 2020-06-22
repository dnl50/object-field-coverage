package de.adesso.test;

import de.adesso.objectfieldcoverage.api.annotation.TestTarget;
import de.adesso.objectfieldcoverage.api.annotation.TestTargets;

public class MelonServiceTest {

    @TestTarget("de.adesso.test.MelonService#saveMelon(de.adesso.test.Melon)")
    void saveMelonQualifiedModelClass() {

    }

    @TestTarget("de.adesso.test.MelonService#deleteMelons(de.adesso.test.Melon[])")
    void deleteMelonsArrayParameter() {

    }

    @TestTarget("de.adesso.test.MelonService#deleteMelons(java.util.List)")
    void deleteMelonsListParameter() {

    }

    @TestTarget("de.adesso.test.MelonService#unboundGenericMethod(Object)")
    void unboundGenericMethod() {

    }

    @TestTarget("de.adesso.test.MelonService#boundGenericMethod(Number)")
    void boundGenericMethod() {

    }

    @TestTarget("de.adesso.test.MelonService#MelonService()")
    void nonExistentDefaultConstructor() {

    }

    @TestTarget("de.unknown.Test#Test()")
    void nonExistentClass() {

    }

    @TestTarget("de.adesso.test.MelonService#unkownMethod()")
    void nonExistentMethod() {

    }

    @TestTarget("de.adesso.test.MelonService#saveMelon(Number)")
    void methodWithParameterTypeNotPresent() {

    }

    @TestTarget("de.adesso.test.MelonService#saveMelon(de.unknown.Test)")
    void parameterTypeNotPartOfModel() {

    }

    @TestTargets({
            @TestTarget("de.adesso.test.MelonService#unboundGenericMethod(Object)"),
            @TestTarget("de.adesso.test.MelonService#boundGenericMethod(Number)")
    })
    void multipleMethods() {

    }

}
