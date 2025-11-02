package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketMessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_message_id")
    private TicketMessage ticketMessage;

    public TicketMessageAttachment(String fileName, TicketMessage ticketMessage) {
        this.fileName = fileName;
        this.ticketMessage = ticketMessage;
    }
}
