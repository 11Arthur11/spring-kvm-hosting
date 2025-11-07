package me.parhamziaei.practice.enums;

public enum Text {

    MAIL_TITLE_EMAIL_VERIFICATION("mail.title.email_verification"),
    MAIL_TITLE_EMAIL_TWO_FACTOR("mail.title.two_factor_code"),
    MAIL_TITLE_FORGOT_PASSWORD("mail.title.forgot_password"),

    TICKET_STATUS_PENDING("ticket.status.pending"),
    TICKET_STATUS_CLOSED("ticket.status.closed"),
    TICKET_STATUS_OPEN("ticket.status.open"),
    TICKET_STATUS_RESOLVED("ticket.status.resolved"),
    TICKET_STATUS_WAITING("ticket.status.waiting");

    private final String key;

    Text(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
