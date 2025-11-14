package me.parhamziaei.practice.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.component.CurrentUser;
import me.parhamziaei.practice.dto.request.query.TicketFilterRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketAdminRequest;
import me.parhamziaei.practice.dto.request.ticket.TicketEditAdminRequest;
import me.parhamziaei.practice.dto.response.ticket.TicketDetailAdminResponse;
import me.parhamziaei.practice.dto.response.ticket.TicketListAdminResponse;
import me.parhamziaei.practice.dto.response.ticket.TicketListUserResponse;
import me.parhamziaei.practice.enums.Message;
import me.parhamziaei.practice.exception.custom.service.NoSuchDataException;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TicketService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tickets")
public class TicketAdminController {

    private final TicketService ticketService;
    private final CurrentUser currentUser;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<?> getAllTickets(
            @ModelAttribute @Valid TicketFilterRequest filterRequest
    ) {
        PagedModel<TicketListAdminResponse> tickets = ticketService.getAllTickets(filterRequest);
        if (tickets.getContent().isEmpty()) {
            throw new NoSuchDataException();
        }
        return ResponseBuilder.buildSuccess(
                "DATA",
                "",
                tickets,
                HttpStatus.OK
        );
    }

    @GetMapping("/{userEmail}")
    public ResponseEntity<?> getAllUserTickets(
            @ModelAttribute TicketFilterRequest filterRequest,
            @PathVariable String userEmail
    ) {
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize());
        PagedModel<TicketListAdminResponse> tickets = ticketService.getUserTickets(
                pageable,
                userEmail,
                TicketListAdminResponse.class
        );
        if (tickets.getContent().isEmpty()) {
            throw new NoSuchDataException();
        }
        return ResponseBuilder.buildSuccess(
                "DATA",
                "Tickets of user: " + userEmail,
                tickets,
                HttpStatus.OK
        );
    }

    @PutMapping("/edit/{ticketId}")
    public ResponseEntity<?> editTicket(
            @Valid @RequestBody TicketEditAdminRequest editRequest,
            @PathVariable Long ticketId
    ) {
        ticketService.editTicket(editRequest, ticketId);
        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                messageService.get(Message.SERVICE_TICKET_EDITED),
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{ticketId}")
    public ResponseEntity<?> getTicketDetails(@PathVariable Long ticketId){
        String userEmail = currentUser.getEmail();
        TicketDetailAdminResponse ticket = ticketService.getTicketDetails(
                ticketId,
                userEmail,
                TicketDetailAdminResponse.class
        );

        return ResponseBuilder.buildSuccess(
                "DATA",
                "",
                ticket,
                HttpStatus.OK
        );
    }

    @Operation(summary = "Submits ticket")
    @PostMapping(
            value = "/submit",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<?> submitTicket(
            @RequestPart("ticket") TicketAdminRequest ticketRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        final String submitterEmail = currentUser.getEmail();
        ticketService.submit(submitterEmail, ticketRequest, files);
        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                messageService.get(Message.SERVICE_TICKET_SUBMITTED),
                HttpStatus.OK
        );
    }
}
