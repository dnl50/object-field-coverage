package de.adesso.test;

public class User {

    // accessible
    private String id;

    // accessible
    private String name;

    // accessible
    private boolean admin;

    // accessible
    private Boolean locked;

    // accessible
    private static String staticString;

    // not accessible (no getter)
    private String privateName;

    // accessible
    private String protectedName;

    // not accessible (wrong return type on public getter)
    private String nameWithGetterWithWrongReturnType;

    // not accessible (public getter is static)
    private String nameWithStaticGetter;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Boolean isLocked() {
        return locked;
    }

    public static String getStaticString() {
        return staticString;
    }

    protected String getProtectedName() {
        return protectedName;
    }

    public int getNameWithGetterWithWrongReturnType() {
        return 1;
    }

    public static String getNameWithStaticGetter() {
        return "";
    }

}
