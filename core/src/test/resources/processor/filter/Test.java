package de.adesso.test;

public class Test extends AbstractTest {

    private final OtherInterface otherInterface = new OtherInterfaceImpl();

    public void test() {
        this.abstractHelperMethod();
        super.abstractHelperMethod();
        Other.other();
        String.valueOf('A');
        otherInterface.interfaceMethod();
        this.helperMethod();
    }

    @Override
    public void abstractHelperMethod() {

    }

    private void helperMethod() {

    }

}
