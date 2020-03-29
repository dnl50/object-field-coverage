package de.adesso.test;

public class ExecutableUtilTest {

    public ExecutableUtilTest() {
        // explicit no-arg default constructor
    }

    public void noArgMethodToInvoke() {
        // do noting
    }

    public void singleArgMethodToInvoke() {
        // do nothing
    }

    public void noArgAndSingleArgMethodNotInvoked() {
        // don't do anything
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

}
