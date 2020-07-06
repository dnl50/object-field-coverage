package de.adesso.test;

public abstract class Pair {

    public abstract int getLeft();

    public abstract int getRight();

    public int getKey() {
        return getLeft();
    }

    public int getValue() {
        return getRight();
    }

}

public class PairImpl extends Pair {

    private int left;

    private int right;

    @Override
    public int getLeft() {
        return left;
    }

    @Override
    public int getRight() {
        return right;
    }

}
