package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;

import java.util.List;
import java.util.Optional;

public interface TicketRepo {

    void save(Ticket ticket);

    void update(Ticket ticket);

    void delete(Ticket ticket);

    Optional<TicketMessage> addMessage(Long ticketId, TicketMessage ticketMessage);

    Optional<TicketMessageAttachment> findTicketAttachmentByAttachmentName(String attachmentName);

    Optional<Ticket> findById(Long id);

    List<Ticket> findAll();

    List<Ticket> findByUserEmail(String userEmail);

}
