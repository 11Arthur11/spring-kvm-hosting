package me.parhamziaei.practice.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketRepoImpl implements TicketRepo {

    private final EntityManager em;

    @Transactional
    @Override
    public void save(Ticket ticket) {
        em.persist(ticket);
    }

    @Transactional
    @Override
    public void update(Ticket ticket) {
        em.merge(ticket);
    }

    @Transactional
    @Override
    public void delete(Ticket ticket) {
        em.remove(ticket);
    }

    @Transactional
    @Override
    public Optional<TicketMessage> addMessage(Long ticketId, TicketMessage ticketMessage) {
        Ticket ticket = em.createQuery("SELECT t FROM Ticket t WHERE t.id =:id", Ticket.class)
                .setParameter("id", ticketId)
                .getSingleResult();

        ticketMessage.setTicket(ticket);
        em.persist(ticketMessage);
        em.flush();
        return Optional.of(ticketMessage);
    }

    @Override
    public Optional<TicketMessageAttachment> findTicketAttachmentByAttachmentName(String fileName) {
        return em.createQuery("SELECT tma FROM TicketMessageAttachment tma WHERE tma.fileName = :fileName", TicketMessageAttachment.class)
                .setParameter("fileName", fileName)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return em.createQuery("SELECT t FROM Ticket t WHERE t.id = :id", Ticket.class).setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Ticket> findAll() {
        return em.createQuery("SELECT t FROM Ticket t", Ticket.class)
                .getResultList();
    }

    @Override
    public List<Ticket> findByUserEmail(String userEmail) {
        return em.createQuery("SELECT t FROM Ticket t WHERE t.submitterEmail = :userEmail", Ticket.class)
                .setParameter("userEmail", userEmail)
                .getResultList();
    }
}
