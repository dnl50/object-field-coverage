package de.adesso.test;

import java.util.Objects;

public class Melon {

    private int seeds = 0;

    private String farmer;

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

    public int getSeeds() {
        return seeds;
    }

    public String getFarmer() {
        return farmer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Melon melon = (Melon) o;
        return seeds == melon.seeds;
    }

}
