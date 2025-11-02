package me.parhamziaei.practice.service;

import jakarta.transaction.Transactional;
import me.parhamziaei.practice.dto.request.TicketMessageRequest;
import me.parhamziaei.practice.dto.request.TicketRequest;
import me.parhamziaei.practice.dto.response.TicketAttachmentResponse;
import me.parhamziaei.practice.dto.response.TicketDetailResponse;
import me.parhamziaei.practice.dto.response.TicketMessageResponse;
import me.parhamziaei.practice.dto.response.TicketResponse;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.TicketMessageAttachment;
import me.parhamziaei.practice.exception.custom.service.TicketMaxAttachmentReachedException;
import me.parhamziaei.practice.exception.custom.service.TicketServiceException;
import me.parhamziaei.practice.repository.jpa.TicketRepo;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepo ticketRepo;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final int maxTicketAttachments;

    public TicketService(TicketRepo ticketRepo, ModelMapper modelMapper, FileStorageService fileStorageService, Environment env) {
        this.ticketRepo = ticketRepo;
        this.modelMapper = modelMapper;
        this.fileStorageService = fileStorageService;
        maxTicketAttachments = Integer.parseInt(env.getProperty("app.ticket.max-message-attachments", "5"));
    }

    @Transactional
    public Optional<String> getAttachmentOwner(String attachmentName) {
        Optional<TicketMessageAttachment> attachment = ticketRepo.findTicketAttachmentByAttachmentName(attachmentName);
        return attachment.map(ticketMessageAttachment -> ticketMessageAttachment.getTicketMessage().getTicket().getSubmitterEmail());
    }

    private boolean isTicketOwner(Optional<Ticket> ticket, String userEmail) {
        return ticket.map(t -> userEmail.equals(t.getSubmitterEmail())).orElse(false);
    }

    @Transactional
    public void addNewMessage(TicketMessageRequest ticketMessageRequest, String email, String senderRole, Long ticketId) {
        if (ticketMessageRequest.getFiles().size() > maxTicketAttachments)  {
            throw new TicketMaxAttachmentReachedException("Maximum number of ticket attachments reached. limit is: " + maxTicketAttachments);
        }
        Optional<Ticket> loadedTicket = ticketRepo.findById(ticketId);
        if (loadedTicket.isPresent() && isTicketOwner(loadedTicket, email)) {
            Ticket ticket = loadedTicket.get();

            TicketMessage newTicketMessage = TicketMessage.builder()
                    .message(ticketMessageRequest.getContent())
                    .senderFullName(ticket.getSubmitterFullName())
                    .build();

            if (ticketMessageRequest.getFiles().isEmpty()) {
                ticket.getMessages().add(newTicketMessage);
                ticketRepo.addMessage(ticket.getId(), newTicketMessage);
            }
            Optional<TicketMessage> dbTicketMessage = ticketRepo.addMessage(ticket.getId(), newTicketMessage);

            dbTicketMessage.ifPresent(loadedTicketMessage -> ticketMessageRequest.getFiles().forEach(file -> {
                String attachmentName = fileStorageService.storeTicketAttachment(file);
                TicketMessageAttachment attachment = new TicketMessageAttachment(attachmentName, loadedTicketMessage);
                loadedTicketMessage.addAttachment(attachment);
            }));

        } else {
            throw new TicketServiceException("Ticket not found or permission denied.");
        }
    }

    @Transactional
    public TicketDetailResponse getTicketDetails(Long id, String userEmail) {
        Optional<Ticket> loadedTicket = ticketRepo.findById(id);
        if (loadedTicket.isPresent() && loadedTicket.get().getSubmitterEmail().equals(userEmail)) {
            Ticket ticket = loadedTicket.get();
            List<TicketMessageResponse> messagesDTO = new ArrayList<>();

            ticket.getMessages().forEach(ticketMessage -> {
                Set<TicketAttachmentResponse> attachmentsDTO = ticketMessage.getAttachments()
                        .stream()
                        .map(att -> new TicketAttachmentResponse(att.getFileName()))
                        .collect(Collectors.toSet());

                TicketMessageResponse messageDTO = modelMapper.map(ticketMessage, TicketMessageResponse.class);
                messageDTO.setAttachments(attachmentsDTO);
                messagesDTO.add(messageDTO);
            });

            List<TicketMessageResponse> sortedMessagesDTO = messagesDTO.stream()
                    .sorted(Comparator.comparing(TicketMessageResponse::getSentAt))
                    .toList();

            TicketDetailResponse ticketDTO = modelMapper.map(ticket, TicketDetailResponse.class);
            ticketDTO.setMessages(sortedMessagesDTO);
            return ticketDTO;
        } else {
            throw new TicketServiceException("Ticket not found or permission missing.");
        }
    }

    @Transactional
    public void addTicket(TicketRequest request) {
        String relatedServiceName = null;
        if (request.getServiceId() != null) {
            //todo make a check if entered service belong to user or not, if not throw TicketServiceException()
        }

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .department(request.getDepartment())
                .serviceName(relatedServiceName)
                .status("PENDING")
                .submitterEmail(request.getSubmitterEmail())
                .submitterFullName(request.getSubmitterFullName())
                .build();

        ticketRepo.save(ticket);
        addNewMessage(request.getMessage(), request.getSubmitterEmail(), "USER", ticket.getId());
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
