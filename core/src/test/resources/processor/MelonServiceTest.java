package de.adesso.test;

import de.adesso.objectfieldcoverage.core.annotation.TestTarget;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MelonServiceTest {

    @Test
    @TestTarget("de.adesso.test.MelonService#hasSeeds()")
    void hasSeedsReturnsTrueWhenSeedsGreaterThanZero() {
        // given
        var testSubject = new MelonService();
        var givenMelon = new Melon(1);

        // when / then
        Assertions.assertEquals(givenMelon, testSubject.saveMelon(givenMelon));
    }

}
