package com.supportai.controller;

import com.supportai.dto.ChatEscalateRequest;
import com.supportai.dto.ChatMessageRequest;
import com.supportai.dto.ChatMessageResponse;
import com.supportai.dto.TicketResponse;
import com.supportai.model.ChatMessage;
import com.supportai.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request,
                                                           Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;

        ChatMessageResponse response = chatService.processMessage(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/escalate")
    public ResponseEntity<TicketResponse> escalateChat(@Valid @RequestBody ChatEscalateRequest request,
                                                       Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;

        TicketResponse ticket = chatService.escalateChatToTicket(request, email);
        return ResponseEntity.ok(ticket);
    }
}
