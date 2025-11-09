package me.parhamziaei.practice.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Optional<TicketMessageAttachment> findTicketAttachmentByStoredName(String storedName) {
        return em.createQuery("SELECT tma FROM TicketMessageAttachment tma WHERE tma.storedName = :storedName", TicketMessageAttachment.class)
                .setParameter("storedName", storedName)
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
    public Page<Ticket> findAll(Pageable pageable) {
        Query query = em.createQuery("FROM Ticket", Ticket.class);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<Ticket> tickets = query.getResultList();
        long totalSize = em.createQuery("SELECT COUNT(t) FROM Ticket t", Long.class).getSingleResult();
        return new PageImpl<>(tickets, pageable, totalSize);
    }

    @Override
    public Page<Ticket> findAllByStatus(TicketStatus status, Pageable pageable) {
        Query query = em.createQuery("SELECT t FROM Ticket t WHERE t.status =:status", Ticket.class);
        query.setParameter("status", status.value());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<Ticket> tickets = query.getResultList();
        long totalSize = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.status =:status", Long.class)
                .setParameter("status", status.value())
                .getSingleResult();
        return new PageImpl<>(tickets, pageable, totalSize);
    }

    @Override
    public Page<Ticket> findAllByDepartment(TicketDepartment department, Pageable pageable) {
        Query query = em.createQuery("SELECT t FROM Ticket t WHERE t.department =:department", Ticket.class);
        query.setParameter("department", department.value());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<Ticket> tickets = query.getResultList();
        long totalSize = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.department =:department", Long.class)
                .setParameter("department", department.value())
                .getSingleResult();
        return new PageImpl<>(tickets, pageable, totalSize);
    }

    @Override
    public Page<Ticket> findAllByStatusAndDepartment(TicketStatus status, TicketDepartment department, Pageable pageable) {
        Query query = em.createQuery("SELECT t FROM Ticket t WHERE t.department =:department AND t.status =:status", Ticket.class);
        query.setParameter("department", department.value());
        query.setParameter("status", status.value());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<Ticket> tickets = query.getResultList();
        long totalSize = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.department =:department AND t.status =:status", Long.class)
                .setParameter("department", department.value())
                .setParameter("status", status.value())
                .getSingleResult();
        return new PageImpl<>(tickets, pageable, totalSize);
    }

    @Override
    public Page<Ticket> findByOwnerEmail(Pageable pageable, String userEmail) {
        Query query = em.createQuery("SELECT t FROM Ticket t WHERE t.ownerEmail = :userEmail", Ticket.class)
                .setParameter("userEmail", userEmail);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());
        List<Ticket> tickets = query.getResultList();
        long totalSize = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.ownerEmail = :userEmail", Long.class)
                .setParameter("userEmail", userEmail)
                .getSingleResult();
        return new PageImpl<>(tickets, pageable, totalSize);
    }
}
