package me.parhamziaei.practice.repository.jpa;

import me.parhamziaei.practice.entity.jpa.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepo {

    void save(Ticket ticket);

    void update(Ticket ticket);

    void delete(Ticket ticket);

    Optional<Ticket> findById(Long id);

    List<Ticket> findAll();

    List<Ticket> findByUserEmail(String userEmail);

}
