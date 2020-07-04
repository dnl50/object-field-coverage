package de.adesso.test;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class AssertThrowsInvocationHandlerIntegrationTest {

    public void junit4AssertThrows() {
        Assert.assertThrows(IllegalStateException.class, () -> new String());
    }

    public void junit4AssertThrowsWithMessage() {
        Assert.assertThrows("Test", IllegalStateException.class, () -> new String());
    }

    public void junit5AssertThrows() {
        Assertions.assertThrows(IllegalStateException.class, () -> new String());
    }

}
