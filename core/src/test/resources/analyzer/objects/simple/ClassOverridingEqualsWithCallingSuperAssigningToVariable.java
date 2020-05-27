package de.adesso.test;

public class ClassOverridingEqualsWithCallingSuperAssigningToVariable {

    @Override
    public boolean equals(Object obj) {
        var result = super.equals(obj);
        return result;
    }

}
