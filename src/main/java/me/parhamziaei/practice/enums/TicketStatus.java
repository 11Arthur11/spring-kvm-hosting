package me.parhamziaei.practice.enums;

import java.util.Arrays;

public enum TicketStatus {

    PENDING("ticket.status.pending", "pending"),
    CLOSED("ticket.status.closed", "closed"),
    RESPONDED("ticket.status.responded", "responded"),
    WAITING("ticket.status.waiting", "waiting");

    private final String key;
    private final String value;

    TicketStatus(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }
    public String value() {
        return value;
    }

    public static TicketStatus fromValue(String value) {
        return Arrays.stream(TicketStatus.values())
                .filter(v -> v.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ticket Department value"));
    }

}
