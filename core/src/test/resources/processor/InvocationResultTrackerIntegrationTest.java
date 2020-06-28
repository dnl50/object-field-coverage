package de.adesso.test;

public class InvocationResultTrackerIntegrationTest {

    public void simpleLocalVariableRead() {
        var result = targetMethod();
        assertNotNull(result);
    }

    public void multiStageAssignmentLocalVariableRead() {
        var result1 = targetMethod();
        var result2 = result1;
        var result3 = result2;
        assertNotNull(result3);
    }

    public void singleMethodInvocationOnInvocation() {
        assertNotNull(targetMethod().getParent());
    }

    public void multipleMethodInvocationsOnInvocation() {
        assertNotNull(targetMethod().getParent().getParent().getParent());
    }

    public void multipleMethodInvocationsAndFieldAccessesOnInvocation() {
        assertNotNull(targetMethod().parent.getParent().parent);
    }

    public void methodInvocationOnLocalVariableContainingInvocationResult() {
        var result = targetMethod();
        assertNotNull(result.getParent());
    }

    public void assertNotNull(Object obj) {
        // body omitted
    }

    public Data targetMethod() {
        return new Data();
    }

    public static class Data {

        public Data parent;

        public Data getParent() {
            return parent;
        }

    }

}
