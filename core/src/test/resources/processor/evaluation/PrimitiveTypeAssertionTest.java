package de.adesso.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrimitiveTypeAssertionTest {

    @Test
    void isTestReturnsTrue() {
        // given
        var testSubject = new PrimitiveTypeAssertionTestTarget();

        // when / then
        Assertions.assertTrue(testSubject.isTest());
    }

}

class PrimitiveTypeAssertionTestTarget {

    public String accessibleString;

    public boolean isTest() {
        return true;
    }

}
