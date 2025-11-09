package me.parhamziaei.practice.controller.admin;

import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.query.TicketFilterRequest;
import me.parhamziaei.practice.dto.response.ticket.TicketListAdminResponse;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.MessageService;
import me.parhamziaei.practice.service.TicketService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tickets")
public class TicketAdminController {

    private final TicketService ticketService;
    private final JwtService jwtService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<?> getAllTickets(
            @ModelAttribute TicketFilterRequest filterRequest
    ) {
        PagedModel<TicketListAdminResponse> tickets = ticketService.getTickets(filterRequest);
        return ResponseBuilder.buildSuccess(
                "DATA",
                "",
                tickets,
                HttpStatus.OK
        );
    }
    
}
