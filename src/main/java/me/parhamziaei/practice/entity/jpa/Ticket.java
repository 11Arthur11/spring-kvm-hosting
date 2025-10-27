package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import me.parhamziaei.practice.enums.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "service_id")
    private String serviceName;

    @Column(name = "submitter_email", nullable = false)
    private String submitterEmail;

    @Column(name = "submitter_full_name")
    private String submitterFullName;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy ="ticket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketMessage> messages = new HashSet<>();

    @Column(columnDefinition = "TIMESTAMP(0)", name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP(0)", name = "updated_at")
    private LocalDateTime lastModified;

    public void addMessage(TicketMessage ticketMessage) {
        ticketMessage.setTicket(this);
        if (messages == null) {
            messages = new HashSet<>();
        }
        messages.add(ticketMessage);
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModified = LocalDateTime.now().withNano(0);
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.lastModified = LocalDateTime.now().withNano(0);
    }

}
