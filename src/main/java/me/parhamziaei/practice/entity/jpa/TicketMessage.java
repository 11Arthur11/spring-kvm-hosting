package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "message")
    private String message;

    @Column(columnDefinition = "TIMESTAMP(0)", name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now().withNano(0);
    }
}
