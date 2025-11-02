package me.parhamziaei.practice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.parhamziaei.practice.dto.request.TicketMessageRequest;
import me.parhamziaei.practice.dto.request.TicketRequest;
import me.parhamziaei.practice.dto.response.TicketDetailResponse;
import me.parhamziaei.practice.dto.response.TicketResponse;
import me.parhamziaei.practice.entity.jpa.Ticket;
import me.parhamziaei.practice.entity.jpa.TicketMessage;
import me.parhamziaei.practice.entity.jpa.User;
import me.parhamziaei.practice.service.JwtService;
import me.parhamziaei.practice.service.TicketService;
import me.parhamziaei.practice.service.UserService;
import me.parhamziaei.practice.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketRestCtrl {

    private final TicketService ticketService;
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<?> getTickets(HttpServletRequest request) {

        final String userEmail = jwtService.extractUsername(jwtService.extractJwtFromRequest(request));
        List<TicketResponse> userTickets = ticketService.getUserTickets(userEmail);

        if (userTickets.isEmpty()) {
            return ResponseBuilder.buildFailed(
                    "NO_DATA",
                    "", //todo language enum reminder!
                    HttpStatus.NOT_FOUND
            );
        }

        return ResponseEntity.ok().body(userTickets);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitTicket(
            @RequestPart("ticket") TicketRequest ticketRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        final User user = (User) userService.loadUserByUsername(jwtService.extractUsername(jwtService.extractJwtFromRequest(request)));
        ticketRequest.setSubmitterEmail(user.getEmail());
        ticketRequest.setSubmitterFullName(user.getFullName());
        ticketRequest.getMessage().setFiles(files);
        ticketService.addTicket(ticketRequest);

        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                "Ticket successfully submitted",
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getTicketDetails(@PathVariable Long id, HttpServletRequest request) {
        final User user = (User) userService.loadUserByUsername(jwtService.extractUsername(jwtService.extractJwtFromRequest(request)));
        TicketDetailResponse ticketDetails = ticketService.getTicketDetails(id, user.getEmail());
        return ResponseEntity.ok().body(ticketDetails);
    }

    @PostMapping("/detail/{ticketId}/message/add")
    public ResponseEntity<?> addTicketMessage(
            @PathVariable Long ticketId,
            @RequestPart("content") String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request
    ) {
        final User user = (User) userService.loadUserByUsername(jwtService.extractUsername(jwtService.extractJwtFromRequest(request)));
        TicketMessageRequest ticketMessageRequest = new TicketMessageRequest();
        ticketMessageRequest.setContent(content);
        if (files != null) {
            ticketMessageRequest.setFiles(files);
        }
        ticketService.addNewMessage(ticketMessageRequest, user.getEmail(), "USER",ticketId);
        return ResponseBuilder.buildSuccess(
                "SUCCESS",
                "", //todo language enum reminder!
                HttpStatus.OK
        );
    }

}
