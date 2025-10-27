package me.parhamziaei.practice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.TicketRequest;
import me.parhamziaei.practice.dto.response.TicketDetailResponse;
import me.parhamziaei.practice.dto.response.TicketMessageResponse;
import me.parhamziaei.practice.dto.response.TicketResponse;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.exception.custom.service.TicketServiceException;
import me.parhamziaei.practice.repository.jpa.TicketRepo;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepo ticketRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public TicketDetailResponse getTicketDetails(Long id, String userEmail) {
        Optional<Ticket> loadedTicket = ticketRepo.findById(id);
        if (loadedTicket.isPresent() && loadedTicket.get().getSubmitterEmail().equals(userEmail)) {
            Ticket ticket = loadedTicket.get();
            Set<TicketMessageResponse> messages = new HashSet<>();

            ticket.getMessages().forEach(ticketMessage -> {
                TicketMessageResponse messageResponse = TicketMessageResponse.builder()
                        .sentAt(ticketMessage.getSentAt())
                        .message(ticketMessage.getMessage())
                        .senderFullName(ticketMessage.getSenderFullName())
                        .build();
                messages.add(messageResponse);
            });

            TicketDetailResponse ticketDTO = modelMapper.map(ticket, TicketDetailResponse.class);
            ticketDTO.setMessages(messages);
            return ticketDTO;
        } else {
            throw new TicketServiceException();
        }
    }

    public void addTicket(TicketRequest request) {
        String relatedServiceName = null;
        if (request.getServiceId() != null) {
            //todo make a check if entered service belong to user or not, if not throw TicketServiceException()
        }

        TicketMessage firstMessage = TicketMessage.builder()
                .message(request.getMessage())
                .senderFullName(request.getSubmitterFullName())
                .build();

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .department(request.getDepartment())
                .serviceName(relatedServiceName)
                .status("PENDING")
                .submitterEmail(request.getSubmitterEmail())
                .submitterFullName(request.getSubmitterFullName())
                .build();

        ticket.addMessage(firstMessage);

        ticketRepo.save(ticket);
    }

    public List<TicketResponse> getUserTickets(String userEmail) {
        List<Ticket> tickets = ticketRepo.findByUserEmail(userEmail)
                .stream()
                .sorted(Comparator.comparing(Ticket::getCreatedAt)).toList();

        List<TicketResponse> ticketsDTO = new ArrayList<>();
        tickets.forEach(ticket -> {
            TicketResponse ticketResponse = modelMapper.map(ticket, TicketResponse.class);
            ticketsDTO.add(ticketResponse);
        });
        return ticketsDTO;
    }

}
