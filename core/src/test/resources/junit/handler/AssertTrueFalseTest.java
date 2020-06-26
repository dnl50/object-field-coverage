package de.adesso.test;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class AssertTrueFalseTest {

    void junit4assertTrueFalse() {
        Assert.assertTrue(true);
        Assert.assertTrue("Message", true);
        Assert.assertFalse(false);
        Assert.assertFalse("Message", true);
    }

    void junit5assertTrueFalse() {
        Assertions.assertTrue(true);
        Assertions.assertTrue(true, "Message");
        Assertions.assertFalse(false);
        Assertions.assertFalse(false, "Message");
        Assertions.assertTrue(() -> true);
        Assertions.assertTrue(() -> true, "Message");
        Assertions.assertFalse(() -> false);
        Assertions.assertFalse(() -> false, "Message");
    }

}
