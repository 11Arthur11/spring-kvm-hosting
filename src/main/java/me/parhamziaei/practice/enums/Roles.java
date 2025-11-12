package me.parhamziaei.practice.enums;

import java.util.*;

public enum Roles {

    USER(0, "ROLE_USER"),
    SUPPORT(1, "ROLE_SUPPORT"),
    ADMIN(2, "ROLE_ADMIN");

    private final int hierarchy;
    private final String value;

    Roles(int order, String name) {
        this.hierarchy = order;
        this.value = name;
    }

    public String nameWithoutPrefix() {
        return this.value.replaceFirst("ROLE_", "");
    }

    public String value(){
        return this.value;
    }
    
    public int hierarchy() {
        return this.hierarchy;
    }

    public static Set<Roles> staffRoles() {
        return Set.of(ADMIN, SUPPORT);
    }
}
