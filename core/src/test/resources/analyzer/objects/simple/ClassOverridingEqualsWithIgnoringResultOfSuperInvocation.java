package de.adesso.test;

public class ClassOverridingEqualsWithIgnoringResultOfSuperInvocation {

    @Override
    public boolean equals(Object obj) {
        super.equals(obj);
        return false;
    }

}
