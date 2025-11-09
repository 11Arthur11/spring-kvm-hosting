package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TicketRepo {

    void save(Ticket ticket);

    void update(Ticket ticket);

    void delete(Ticket ticket);

    Optional<TicketMessage> addMessage(Long ticketId, TicketMessage ticketMessage);

    Optional<TicketMessageAttachment> findTicketAttachmentByStoredName(String storedName);

    Optional<Ticket> findById(Long id);

    Page<Ticket> findAll(Pageable pageable);

    Page<Ticket> findAllByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findAllByDepartment(TicketDepartment department, Pageable pageable);

    Page<Ticket> findAllByStatusAndDepartment(TicketStatus status, TicketDepartment department, Pageable pageable);

    Page<Ticket> findByOwnerEmail(Pageable pageable, String userEmail);

}
