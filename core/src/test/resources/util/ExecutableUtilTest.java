package de.adesso.test;

import de.adesso.objectfieldcoverage.api.annotation.TestTarget;
import de.adesso.objectfieldcoverage.api.annotation.TestTargets;

public class ExecutableUtilTest {

    public ExecutableUtilTest() {
        // explicit no-arg default constructor
    }

    public void noArgMethodToInvoke() {
        // do nothing
    }

    public void singleArgMethodToInvoke() {
        // do nothing
    }

    public void noArgAndSingleArgMethodNotInvoked() {
        // do nothing
    }

    public void noArgMethodInvokedOnce() {
        this.noArgMethodToInvoke();
    }

    public void noArgMethodInvokedTwice() {
        this.noArgMethodToInvoke();
        this.noArgMethodToInvoke();
    }

    public void singleArgMethodInvokedOnce() {
        this.singleArgMethodToInvoke();
    }

    public void singleArgMethodInvokedTwice() {
        this.singleArgMethodToInvoke();
        this.singleArgMethodToInvoke();
    }

    public void noArgAndSingleArgMethodInvokedOnce() {
        this.noArgMethodToInvoke();
        this.singleArgMethodToInvoke();
    }

    public void voidMethod() {

    }

    public Void voidTypeMethod() {
        return null;
    }

    public int intPrimitiveType() {
        return 0;
    }

    public boolean booleanPrimitiveType() {
        return false;
    }

    @TestTarget("de.adesso.test.UnknownClass#unknownMethod()")
    public void singleUnknownTestTarget() {
        // do nothing
    }

    @TestTargets({})
    public void emptyTestTargets() {
        // do nothing
    }

    @TestTarget("de.adesso.test.ExecutableUtilTest#voidMethod()")
    public void singleKnownVoidTestTarget() {
        // do nothing
    }

    @TestTarget(value = "de.adesso.test.ExecutableUtilTest#voidMethod()")
    public void singleKnownVoidTestTargetWithFlagSet() {
        // do nothing
    }

    @TestTarget("de.adesso.test.ExecutableUtilTest#intPrimitiveType()")
    public void singleKnownNonVoidTestTarget() {
        // do nothing
    }

    @TestTargets({
            @TestTarget("de.adesso.test.ExecutableUtilTest#intPrimitiveType()"),
            @TestTarget("de.adesso.test.ExecutableUtilTest#booleanPrimitiveType()")
    })
    public void multipleKnownNonVoidTestTarget() {
        // do nothing
    }

}
