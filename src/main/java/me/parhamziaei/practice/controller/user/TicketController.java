package me.parhamziaei.practice.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.internal.ImageInternal;
import me.parhamziaei.practice.dto.query.TicketFilterRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketMessageRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketRequest;
import me.parhamziaei.practice.dto.response.ticket.TicketDetailResponse;
import me.parhamziaei.practice.dto.response.ticket.TicketListUserResponse;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TicketService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final JwtService jwtService;
    private final MessageService messageService;

    @Operation(summary = "Getting all user tickets as list")
    @GetMapping
    public ResponseEntity<?> getTickets(
            @ModelAttribute TicketFilterRequest filterRequest,
            HttpServletRequest request
    ) {
        final String userEmail = jwtService.extractUsername(request);
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(filterRequest.getSortedBy()).ascending()
        );

        PagedModel<TicketListUserResponse> userTickets = ticketService.getUserTickets(pageable, userEmail);
        if (userTickets.getContent().isEmpty()) {
            return ResponseBuilder.buildFailed(
                    "NO_DATA",
                    null,
                    HttpStatus.OK
            );
        }

        return ResponseBuilder.buildSuccess(
                "DATA",
                null,
                userTickets,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Submits ticket")
    @PostMapping(
            value = "/submit",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<?> submitTicket(
            @RequestPart("ticket") TicketRequest ticketRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        final String userEmail = jwtService.extractUsername(request);
        ticketService.submitTicket(userEmail, ticketRequest, files);
        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                messageService.get(Message.SERVICE_TICKET_SUBMITTED),
                HttpStatus.OK
        );
    }

    @Operation(summary = "Ticket Details with all messages")
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getTicketDetails(@PathVariable Long id, HttpServletRequest request) {
        final String userEmail = jwtService.extractUsername(request);
        TicketDetailResponse ticketDetails = ticketService.getTicketDetails(id, userEmail);
        return ResponseBuilder.buildSuccess(
                "DATA",
                null,
                ticketDetails,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Adding new message to existing ticket")
    @PostMapping(
            value = "/detail/{ticketId}/message/add",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<?> addTicketMessage(
            @PathVariable Long ticketId,
            @RequestPart("content") String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        final String userEmail = jwtService.extractUsername(request);
        TicketMessageRequest ticketMessageRequest = new TicketMessageRequest(content);
        ticketService.addNewMessage(ticketMessageRequest, userEmail, ticketId, files);
        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                messageService.get(Message.SERVICE_TICKET_MESSAGE_SENT),
                HttpStatus.OK
        );
    }

    @GetMapping("/attachment/{identifier}")
    public ResponseEntity<?> getAttachment(@PathVariable String identifier, HttpServletRequest request) {
        final String userEmail = jwtService.extractUsername(request);
        ImageInternal image = ticketService.getTicketAttachment(identifier, userEmail);
        return ResponseBuilder.buildImageResponse(image);
    }

}
