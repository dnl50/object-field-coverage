package de.adesso.test;

import de.adesso.objectfieldcoverage.annotation.TestTarget;
import de.adesso.test.Melon;

public class MelonTest {

    @TestTarget("de.adesso.test.Melon#hasSeeds()")
    void hasSeedsReturnsTrueWhenSeedsGreaterThanZero() {

    }

    @TestTarget("de.adesso.test.Melon#Melon(int)")
    void constructorWithPrimitiveParameter() {

    }

    @TestTarget("de.adesso.test.Melon#incrementSeeds()")
    void incrementSeedsNoParameter() {

    }

    @TestTarget("de.adesso.test.Melon#incrementSeeds(int)")
    void incrementSeedsPrimitiveTypeParameter() {

    }

    @TestTarget("de.adesso.test.Melon#incrementSeeds(java.lang.String)")
    void incrementSeedsQualifiedJavaLangParameter() {

    }

    @TestTarget("de.adesso.test.Melon#incrementSeeds(String)")
    void incrementSeedsNonQualifiedJavaLangParameter() {

    }

    @TestTarget("de.adesso.test.Melon#incrementSeeds(String, java.lang.String)")
    void incrementSeedsQualifiedAndNonQualifiedJavaLangParameters() {

    }

}
