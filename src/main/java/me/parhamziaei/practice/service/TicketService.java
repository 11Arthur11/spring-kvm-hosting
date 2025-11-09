package me.parhamziaei.practice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.configuration.properties.TicketServiceProperties;
import me.parhamziaei.practice.dto.internal.ImageInternal;
import me.parhamziaei.practice.dto.query.TicketFilterRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketMessageRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketRequest;
import me.parhamziaei.practice.dto.response.ticket.*;
import me.parhamziaei.practice.entity.jpa.*;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import me.parhamziaei.practice.exception.custom.service.TicketMaxAttachmentReachedException;
import me.parhamziaei.practice.exception.custom.service.TicketServiceException;
import me.parhamziaei.practice.repository.jpa.TicketRepo;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepo ticketRepo;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final MessageService messageService;
    private final TicketServiceProperties properties;

    private boolean isTicketOwner(Optional<Ticket> ticket, String userEmail) {
        return ticket.map(t -> userEmail.equals(t.getSubmitterEmail())).orElse(false);
    }

    private boolean isAttachmentOwner(TicketMessageAttachment attachment, String userEmail) {
        return attachment.getOwnerEmail().equals(userEmail);
    }

    private <T extends TicketBaseResponse> T enrichTicket(Ticket ticket, T dto) {
        dto.setDepartment(messageService.get(TicketDepartment.fromValue(ticket.getDepartment())));
        dto.setStatus(messageService.get(TicketStatus.fromValue(ticket.getStatus())));
        return dto;
    }

    public PagedModel<TicketListAdminResponse> getTickets(TicketFilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), Sort.by(filterRequest.getSortedBy()).ascending());
        Page<Ticket> ticketPage;

        boolean isAllStatus = filterRequest.getStatus().equals("all");
        boolean isAllDepartment = filterRequest.getDepartment().equals("all");

        if (isAllStatus && isAllDepartment) {
            ticketPage = ticketRepo.findAll(pageable);
        } else if (isAllStatus) {
            TicketDepartment department = TicketDepartment.fromValue(filterRequest.getDepartment());
            ticketPage = ticketRepo.findAllByDepartment(department, pageable);
        } else if (isAllDepartment) {
            TicketStatus status = TicketStatus.fromValue(filterRequest.getStatus());
            ticketPage = ticketRepo.findAllByStatus(status, pageable);
        } else {
            TicketDepartment department = TicketDepartment.fromValue(filterRequest.getDepartment());
            TicketStatus status = TicketStatus.fromValue(filterRequest.getStatus());
            ticketPage = ticketRepo.findAllByStatusAndDepartment(status, department, pageable);
        }

        List<TicketListAdminResponse> ticketsDTO = new ArrayList<>();
        ticketPage.getContent()
                .forEach(t -> {
                    TicketListAdminResponse ticketDTO = enrichTicket(t, modelMapper.map(t, TicketListAdminResponse.class));
                    ticketsDTO.add(ticketDTO);
                });

        Page<TicketListAdminResponse> ticketsDTOPage = new PageImpl<>(ticketsDTO, pageable, ticketPage.getTotalElements());
        return new PagedModel<>(ticketsDTOPage);
    }

    @Transactional
    public void addNewMessage(TicketMessageRequest ticketMessageRequest, String senderEmail, Long ticketId, List<MultipartFile> files) {
        if (files == null) {
            files = new ArrayList<>();
        }
        if (files.size() > properties.maxAttachmentPerMessage())  {
            throw new TicketMaxAttachmentReachedException("Maximum number of ticket attachments reached. limit is: " + properties.maxAttachmentPerMessage());
        }
        Optional<Ticket> loadedTicket = ticketRepo.findById(ticketId);
        if (loadedTicket.isPresent() && isTicketOwner(loadedTicket, senderEmail)) {
            Ticket ticket = loadedTicket.get();
            User senderUser = (User) userService.loadUserByUsername(senderEmail);

            String senderRole = senderUser
                    .getRoles()
                    .stream()
                    .findFirst()
                    .map(Role::getName)
                    .orElse(null);

            TicketMessage newTicketMessage = TicketMessage.builder()
                    .message(ticketMessageRequest.getContent())
                    .senderFullName(senderUser.getFullName())
                    .senderRole(senderRole)
                    .build();

            if (files.isEmpty()) {
                ticketRepo.addMessage(ticket.getId(), newTicketMessage);
                return;
            }

            Optional<TicketMessage> dbTicketMessage = ticketRepo.addMessage(ticket.getId(), newTicketMessage);

            List<MultipartFile> finalFiles = files;
            dbTicketMessage.ifPresent(loadedTicketMessage -> finalFiles.forEach(file -> {
                String storedPath = fileStorageService.storeTicketAttachment(file);

                TicketMessageAttachment attachment = TicketMessageAttachment.builder()
                        .originalName(file.getOriginalFilename())
                        .storedPath(storedPath)
                        .storedName(Paths.get(storedPath).getFileName().toString())
                        .size(file.getSize())
                        .mimeType(file.getContentType())
                        .ownerEmail(senderEmail)
                        .ticketMessage(loadedTicketMessage)
                        .build();

                loadedTicketMessage.addAttachment(attachment);
            }));

        } else {
            throw new TicketServiceException("Ticket not found or permission missing.");
        }
    }

    @Transactional
    public TicketDetailResponse getTicketDetails(Long id, String requesterEmail) {
        Optional<Ticket> loadedTicket = ticketRepo.findById(id);
        if (loadedTicket.isPresent() && isTicketOwner(loadedTicket, requesterEmail)) {
            Ticket ticket = loadedTicket.get();
            List<TicketMessageResponse> messagesDTO = new ArrayList<>();

            ticket.getMessages().forEach(ticketMessage -> {
                Set<TicketAttachmentResponse> attachmentsDTO = ticketMessage.getAttachments()
                        .stream()
                        .map(att ->
                                TicketAttachmentResponse.builder()
                                        .attachmentName(att.getOriginalName())
                                        .size(att.getSize())
                                        .identifier(att.getStoredName())
                                        .build()
                        )
                        .collect(Collectors.toSet());

                TicketMessageResponse messageDTO = modelMapper.map(ticketMessage, TicketMessageResponse.class);
                messageDTO.setAttachments(attachmentsDTO);
                messagesDTO.add(messageDTO);
            });

            List<TicketMessageResponse> sortedMessagesDTO = messagesDTO.stream()
                    .sorted(Comparator.comparing(TicketMessageResponse::getSentAt))
                    .toList();

            TicketDetailResponse ticketDTO = enrichTicket(ticket, modelMapper.map(ticket, TicketDetailResponse.class));
            ticketDTO.setMessages(sortedMessagesDTO);
            return ticketDTO;
        } else {
            throw new TicketServiceException("Ticket not found or permission missing.");
        }
    }

    @Transactional
    public void submitTicket(String submitterEmail, TicketRequest request, List<MultipartFile> files) {
        String relatedServiceName = null;
        User submitterUser = (User) userService.loadUserByUsername(submitterEmail);
        if (request.getServiceId() != null) {
            //todo make a check if entered service belong to ownerEmail or not, if not throw TicketServiceException()
        }
        String ticketDepartment = TicketDepartment.validateValue(request.getDepartment());

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .department(ticketDepartment)
                .serviceName(relatedServiceName)
                .status(TicketStatus.PENDING.value())
                .ownerEmail(submitterEmail)
                .submitterEmail(submitterUser.getEmail())
                .ownerFullName(submitterUser.getFullName())
                .build();

        ticketRepo.save(ticket);
        addNewMessage(
                request.getMessage(),
                submitterUser.getEmail(),
                ticket.getId(),
                files
        );
    }

    @Transactional
    public void submitTicketByAdmin(String submitterEmail, String ownerEmail, TicketRequest request, List<MultipartFile> files) {
        String relatedServiceName = null;
        User submitterUser = (User) userService.loadUserByUsername(submitterEmail);
        User ownerUser = (User) userService.loadUserByUsername(ownerEmail);
        if (request.getServiceId() != null) {
            //todo make a check if entered service belong to ownerEmail or not, if not throw TicketServiceException()
        }
        String ticketDepartment = TicketDepartment.validateValue(request.getDepartment());

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .department(ticketDepartment)
                .serviceName(relatedServiceName)
                .status(TicketStatus.WAITING.value())
                .ownerEmail(ownerUser.getEmail())
                .ownerFullName(ownerUser.getFullName())
                .submitterEmail(submitterUser.getEmail())
                .build();

        ticketRepo.save(ticket);
        addNewMessage(
                request.getMessage(),
                submitterUser.getEmail(),
                ticket.getId(),
                files
        );
    }

    public PagedModel<TicketListUserResponse> getUserTickets(Pageable pageable,String userEmail) {
        Page<Ticket> tickets = ticketRepo.findByOwnerEmail(pageable, userEmail);

        List<TicketListUserResponse> ticketsDTO = new ArrayList<>();
        tickets.getContent().forEach(ticket -> {
            TicketListUserResponse ticketDTO = enrichTicket(ticket, modelMapper.map(ticket, TicketListUserResponse.class));
            ticketsDTO.add(ticketDTO);
        });

        Page<TicketListUserResponse> sortedPageDTO = new PageImpl<>(ticketsDTO, pageable, tickets.getTotalElements());
        return new PagedModel<>(sortedPageDTO);
    }

    public ImageInternal getTicketAttachment(String attachmentIdentifier, String userEmail) {
        Optional<TicketMessageAttachment> dbAttachment = ticketRepo.findTicketAttachmentByStoredName(attachmentIdentifier);
        User user = (User) userService.loadUserByUsername(userEmail);
        if (dbAttachment.isPresent()) {
            TicketMessageAttachment attachment = dbAttachment.get();
            Optional<Resource> imageResource = fileStorageService.loadTicketAttachment(attachment);
            if (isAttachmentOwner(attachment, user.getEmail()) && imageResource.isPresent()) {
                return ImageInternal.builder()
                        .image(imageResource.get())
                        .originalName(attachment.getOriginalName())
                        .size(attachment.getSize())
                        .mimeType(attachment.getMimeType())
                        .storedName(attachment.getStoredName())
                        .build();
            }
        }
        log.warn("Ticket attachment not found or permission missing, user: {}, fileName: {}", userEmail, attachmentIdentifier);
        throw new TicketServiceException("Attachment not found or permission denied.");
    }
}
