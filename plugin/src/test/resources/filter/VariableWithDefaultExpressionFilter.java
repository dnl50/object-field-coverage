package de.adesso.test;

public class VariableWithDefaultExpressionFilter {

    public void uninitializedVariable() {
        String test;
    }

    public void initializedVariable() {
        String test = "";
        Integer.max(0, 1);
    }

}
