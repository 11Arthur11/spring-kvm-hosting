package me.parhamziaei.practice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.parhamziaei.practice.configuration.properties.TicketServiceProperties;
import me.parhamziaei.practice.dto.internal.ImageInternal;
import me.parhamziaei.practice.dto.request.query.TicketFilterRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketBaseRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketEditAdminRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketMessageRequest;
import me.parhamziaei.practice.dto.response.ticket.*;
import me.parhamziaei.practice.entity.jpa.*;
import me.parhamziaei.practice.enums.TicketDepartment;
import me.parhamziaei.practice.enums.TicketStatus;
import me.parhamziaei.practice.exception.custom.service.NoSuchDataException;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

// reminder make sure you send an email or sms on each ticket sending or modified phase.

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

    protected BiPredicate<User, Ticket> hasAccessToTicket = (user, ticket) -> {
        if (user.isStaff())
            return true;
        else
            return ticket.getOwnerEmail().equals(user.getEmail());
    };

    protected BiPredicate<User, TicketMessageAttachment> hasAccessToAttachment = (user, attachment) -> {
        if (user.isStaff())
            return true;
        else
            return attachment.getOwnerEmail().equals(user.getEmail());
    };

    private <T extends AbstractTicketResponse> T enrichTicket(Ticket ticket, T dto) {
        dto.setDepartment(messageService.get(TicketDepartment.fromValue(ticket.getDepartment())));
        dto.setStatus(messageService.get(TicketStatus.fromValue(ticket.getStatus())));
        return dto;
    }

    private <T extends AbstractTicketResponse> Page<T> mapPage(Page<Ticket> source, Class<T> responseType) {
        List<T> list = new ArrayList<>();
        source.getContent().forEach(t -> {
            T dto = enrichTicket(t, modelMapper.map(t, responseType));
            list.add(dto);
        });
        return new PageImpl<>(list, source.getPageable(), source.getTotalElements());
    }

    private <T extends AbstractTicketResponse> T mapTicket(Ticket ticket, Class<T> responseType) {
        return enrichTicket(ticket, modelMapper.map(ticket, responseType));
    }

    private <T extends TicketDetailBaseResponse> T mapTicketDetail(Ticket ticket, Class<T> responseType) {
        List<TicketMessageResponse> messagesDTO = mapMessages(ticket.getMessages());
        T dto = enrichTicket(ticket, modelMapper.map(ticket, responseType));
        dto.setMessages(messagesDTO);
        return dto;
    }

    private List<TicketMessageResponse> mapMessages(Set<TicketMessage> ticketMessage) {
        List<TicketMessageResponse> messagesDTO = new ArrayList<>();

        ticketMessage.forEach(tm -> {
            Set<TicketAttachmentResponse> attachmentsDTO = tm.getAttachments()
                    .stream()
                    .map(att ->
                            TicketAttachmentResponse.builder()
                                    .attachmentName(att.getOriginalName())
                                    .size(att.getSize())
                                    .identifier(att.getStoredName())
                                    .build()
                    )
                    .collect(Collectors.toSet());

            TicketMessageResponse messageDTO = modelMapper.map(tm, TicketMessageResponse.class);
            messageDTO.setAttachments(attachmentsDTO);
            messagesDTO.add(messageDTO);
        });

        return messagesDTO.stream()
                .sorted(Comparator.comparing(TicketMessageResponse::getSentAt))
                .toList();
    }

    public PagedModel<TicketListAdminResponse> getAllTickets(TicketFilterRequest filterRequest) {
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), Sort.by(filterRequest.getSortedBy()).ascending());
        Page<Ticket> ticketPage;

        boolean isAllStatus = filterRequest.getStatus() == null;
        boolean isAllDepartment = filterRequest.getDepartment() == null;

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

        Page<TicketListAdminResponse> dtoPage = mapPage(ticketPage, TicketListAdminResponse.class);
        return new PagedModel<>(dtoPage);
    }

    @Transactional
    public void addNewMessage(TicketMessageRequest ticketMessageRequest, String senderUserEmail, Long ticketId, List<MultipartFile> files) {
        User user = (User) userService.loadUserByUsername(senderUserEmail);
        addNewMessage(ticketMessageRequest, user, ticketId, files);
    }

    protected BiFunction<TicketStatus, User, TicketStatus> calculateNewStatus = (currentStatus, modifierUser) -> {
        switch (currentStatus) {
            case CLOSED -> {
                if (!modifierUser.isStaff())
                    throw new TicketServiceException("Cannot add new message on closed ticket");
                else
                    return TicketStatus.WAITING;
            }
            case PENDING -> {
                if (modifierUser.isStaff())
                    return TicketStatus.WAITING;
            }
            case RESPONDED, WAITING -> {
                if (!modifierUser.isStaff())
                    return TicketStatus.PENDING;
            }
        }
        return currentStatus;
    };

    public void changeTicketStatus(Long ticketId, TicketStatus newStatus) {
        Optional<Ticket> dbTicket = ticketRepo.findById(ticketId);
        if (dbTicket.isPresent()) {
            Ticket ticket = dbTicket.get();
            ticket.setStatus(newStatus.value());
            ticketRepo.update(ticket);
        }
    }

    public void editTicket(TicketEditAdminRequest request, Long ticketId) {
        Optional<Ticket> dbTicket = ticketRepo.findById(ticketId);
        if (dbTicket.isPresent()) {
            Ticket ticket = dbTicket.get();

            if (request.getNewSubject() != null)
                ticket.setSubject(request.getNewSubject());
            if (request.getNewStatus() != null)
                ticket.setStatus(request.getNewStatus());
            if (ticket.getStatus() != null)
                ticket.setStatus(ticket.getStatus());

            ticketRepo.update(ticket);
        }
    }

    @Transactional
    protected void addNewMessage(TicketMessageRequest ticketMessageRequest, User senderUser, Long ticketId, List<MultipartFile> files) {
        if (files != null && files.size() > properties.maxAttachmentPerMessage())  {
            throw new TicketMaxAttachmentReachedException("Maximum number of ticket attachments reached. limit is: " + properties.maxAttachmentPerMessage());
        }
        Optional<Ticket> loadedTicket = ticketRepo.findById(ticketId);
        if (loadedTicket.isPresent() && hasAccessToTicket.test(senderUser, loadedTicket.get())) {
            Ticket ticket = loadedTicket.get();

            String senderRole = senderUser.getHigherAuthority().getName();
            TicketStatus newStatus = calculateNewStatus.apply(
                    TicketStatus.fromValue(ticket.getStatus()),
                    senderUser
            );

            if (!newStatus.equals(TicketStatus.fromValue(ticket.getStatus()))) {
                changeTicketStatus(ticket.getId(), newStatus);
            }

            TicketMessage newTicketMessage = TicketMessage.builder()
                    .message(ticketMessageRequest.getContent())
                    .senderFullName(senderUser.getFullName())
                    .senderRole(senderRole)
                    .build();

            addAttachmentsToTicketMessage(
                    ticketRepo.addMessage(ticketId, newTicketMessage),
                    files
            );

        } else {
            throw new TicketServiceException("Ticket not found with id " + ticketId);
        }
    }

    @Transactional
    protected void addAttachmentsToTicketMessage(Optional<TicketMessage> ticketMessage, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        ticketMessage.ifPresent(loadedTicketMessage -> files.forEach(file -> {
            String storedPath = fileStorageService.storeTicketAttachment(file);

            TicketMessageAttachment attachment = TicketMessageAttachment.builder()
                    .originalName(file.getOriginalFilename())
                    .storedPath(storedPath)
                    .storedName(Paths.get(storedPath).getFileName().toString())
                    .size(file.getSize())
                    .mimeType(file.getContentType())
                    .ownerEmail(loadedTicketMessage.getTicket().getOwnerEmail())
                    .ticketMessage(loadedTicketMessage)
                    .build();

            loadedTicketMessage.addAttachment(attachment);
        }));
    }

    public <T extends TicketDetailBaseResponse> T getTicketDetails(Long ticketId, String requesterEmail, Class<T> responseType) {
        User requesterUser = (User) userService.loadUserByUsername(requesterEmail);
        Optional<Ticket> loadedTicket = ticketRepo.findById(ticketId);
        if (loadedTicket.isPresent() && hasAccessToTicket.test(requesterUser, loadedTicket.get())) {
            Ticket ticket = loadedTicket.get();
            return mapTicketDetail(ticket, responseType);
        } else {
            throw new NoSuchDataException("Ticket not found with id " + ticketId);
        }
    }

    @Transactional
    public <T extends TicketBaseRequest> void submit(String submitterEmail, T ticketRequest, List<MultipartFile> files) {
        String relatedServiceName = null;
        User submitterUser = (User) userService.loadUserByUsername(submitterEmail);
        if (ticketRequest.getServiceName() != null) {
            //todo make a check if entered service belong to ownerEmail or not, if not throw TicketServiceException()
        }
        String ticketDepartment = ticketRequest.getDepartment();

        Ticket ticket = Ticket.builder()
                .subject(ticketRequest.getSubject())
                .department(ticketDepartment)
                .serviceName(relatedServiceName)
                .submitterEmail(submitterUser.getEmail())
                .ownerFullName(submitterUser.getFullName())
                .build();

        if (submitterUser.isStaff()) {
            ticket.setStatus(TicketStatus.WAITING.value());
            ticket.setOwnerEmail(ticketRequest.getOwnerEmail());
        } else {
            ticket.setStatus(TicketStatus.PENDING.value());
            ticket.setOwnerEmail(submitterEmail);
        }

        ticketRepo.save(ticket);
        addNewMessage(
                ticketRequest.getMessage(),
                submitterUser,
                ticket.getId(),
                files
        );

    }

    public <T extends AbstractTicketResponse> PagedModel<T> getUserTickets(Pageable pageable, String userEmail, Class<T> responseType) {
        Page<Ticket> tickets = ticketRepo.findByOwnerEmail(pageable, userEmail);
        Page<T> sortedPageDTO = mapPage(tickets, responseType);
        return new PagedModel<>(sortedPageDTO);
    }

    public ImageInternal getTicketAttachment(String attachmentIdentifier, String userEmail) {
        Optional<TicketMessageAttachment> dbAttachment = ticketRepo.findTicketAttachmentByStoredName(attachmentIdentifier);
        User user = (User) userService.loadUserByUsername(userEmail);
        if (dbAttachment.isPresent()) {
            TicketMessageAttachment attachment = dbAttachment.get();
            Optional<Resource> imageResource = fileStorageService.loadTicketAttachment(attachment);
            if (hasAccessToAttachment.test(user, attachment) && imageResource.isPresent()) {
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
