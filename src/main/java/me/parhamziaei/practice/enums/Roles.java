package me.parhamziaei.practice.enums;

public enum Roles {

    ADMIN("ROLE_ADMIN"),
    SUPPORT("ROLE_SUPPORT"),
    USER("ROLE_USER");

    private final String name;

    Roles(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
