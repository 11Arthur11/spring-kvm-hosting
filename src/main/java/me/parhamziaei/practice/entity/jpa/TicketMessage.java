package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_full_name")
    private String senderFullName;

    @Column(name = "sender_role")
    private String senderRole;

    @Column(name = "message")
    private String message;

    @OneToMany(mappedBy ="ticketMessage", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<TicketMessageAttachment> attachments = new HashSet<>();

    @Column(columnDefinition = "TIMESTAMP(0)", name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now().withNano(0);
    }

    public void addAttachment(TicketMessageAttachment attachment) {
        attachment.setTicketMessage(this);
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        this.attachments.add(attachment);
    }
}
