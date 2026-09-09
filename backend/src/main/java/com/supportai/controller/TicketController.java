package com.supportai.controller;

import com.supportai.dto.TicketMessageRequest;
import com.supportai.dto.TicketRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.model.TicketStatus;
import com.supportai.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request,
                                                       Authentication authentication) {
        String email = authentication.getName();
        TicketResponse response = ticketService.createTicket(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTickets(Authentication authentication) {
        String email = authentication.getName();
        List<TicketResponse> tickets = ticketService.getTicketsForUser(email);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        TicketResponse ticket = ticketService.getTicketById(id, email);
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        TicketStatus status = TicketStatus.valueOf(statusStr);
        TicketResponse updated = ticketService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponse> assignAgent(@PathVariable Long id,
                                                      @RequestBody Map<String, Long> body) {
        Long agentId = body.get("agentId");
        TicketResponse updated = ticketService.assignAgent(id, agentId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<TicketResponse> addMessage(@PathVariable Long id,
                                                     @Valid @RequestBody TicketMessageRequest request,
                                                     Authentication authentication) {
        String email = authentication.getName();
        TicketResponse response = ticketService.addMessage(id, request, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<TicketResponse> escalateTicket(@PathVariable Long id) {
        TicketResponse response = ticketService.escalateTicket(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/ai-suggestion")
    @PreAuthorize("hasAnyRole('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> getAiSuggestedReply(@PathVariable Long id) {
        String draft = ticketService.getAiSuggestedReply(id);
        return ResponseEntity.ok(Map.of("suggestedReply", draft));
    }
}
