package de.adesso.test;

public class ClassOverridingEqualsWithCallingSuper {

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
