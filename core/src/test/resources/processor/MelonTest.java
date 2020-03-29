package de.adesso.test;

import de.adesso.objectfieldcoverage.core.annotation.TestTarget;
import de.adesso.test.Melon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MelonTest {

    @Test
    @TestTarget("de.adesso.test.Melon#hasSeeds()")
    void hasSeedsReturnsTrueWhenSeedsGreaterThanZero() {
        /// given
        var testMelon = new Melon(1);
        var expectedResult = true;

        // when / then
        Assertions.assertEquals(expectedResult, testMelon.hasSeeds());
    }

}
