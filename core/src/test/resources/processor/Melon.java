package de.adesso.test;

public class Melon {

    private int seeds = 0;

    public Melon(int seeds) {
        this.seeds = seeds;
    }

    public int incrementSeeds() {
        return ++seeds;
    }

    public int incrementSeeds(int i) {
        return seeds += i;
    }

    public int incrementSeeds(String s) {
        return seeds += Integer.parseInt(s);
    }

    public int incrementSeeds(String s1, String s2) {
        return seeds += (Integer.parseInt(s1) + Integer.parseInt(s2));
    }

    public boolean hasSeeds() {
        return seeds > 0;
    }

    public void doNothing() {

    }

}
