package de.adesso.test;

public class Person {

    private String name;

    // loop Person <-> Person
    private Person sibling;

    private Address homeAddress;

    // multiple ways to access City#name
    //     1: favouriteCity field -> getName() method
    //     2: homeAddress field -> getCity() method -> getName() method
    private City favouriteCity;

    // -------------------------
    // -------- Getters --------
    // -------------------------

    public String getName() {
        return name;
    }

    public Person getSibling() {
        return sibling;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public City getFavouriteCity() {
        return favouriteCity;
    }

}
