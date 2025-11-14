package me.parhamziaei.practice.enums;

import java.util.Arrays;

public enum TicketDepartment {

    TECHNICAL("ticket.department.technical", "technical"),
    SALES("ticket.department.sales", "sales");

    private final String key;
    private final String value;

    TicketDepartment(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public static TicketDepartment fromValue(String value) {
        return Arrays.stream(TicketDepartment.values())
                .filter(v -> v.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ticket Department value"));
    }
}
