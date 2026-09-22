package com.supportai.service;

import com.supportai.ai.AIService;
import com.supportai.ai.SentimentResult;
import com.supportai.dto.ChatEscalateRequest;
import com.supportai.dto.ChatMessageRequest;
import com.supportai.dto.ChatMessageResponse;
import com.supportai.dto.TicketRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.model.*;
import com.supportai.repository.ChatMessageRepository;
import com.supportai.repository.ChatSessionRepository;
import com.supportai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private FaqService faqService;

    @Autowired
    private TicketService ticketService;

    @Transactional
    public ChatMessageResponse processMessage(ChatMessageRequest request, String userEmail) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        final String finalSessionId = sessionId;
        ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession(finalSessionId, null, request.getCustomerName());
                    if (userEmail != null) {
                        userRepository.findByEmail(userEmail).ifPresent(u -> {
                            newSession.setUserId(u.getId());
                            newSession.setCustomerName(u.getFullName());
                        });
                    }
                    return sessionRepository.save(newSession);
                });

        // 1. Analyze Sentiment
        SentimentResult sentiment = aiService.analyzeSentiment(request.getMessage());

        // 2. Save Customer message
        ChatMessage userMsg = new ChatMessage(
                sessionId,
                SenderType.CUSTOMER,
                request.getMessage(),
                sentiment.getLabel()
        );
        messageRepository.save(userMsg);

        // 3. Search FAQs for Grounding
        List<FaqArticle> relevantFaqs = faqService.searchFaqs(request.getMessage());

        // 4. Check for Escalation / Complaint Registration
        boolean shouldEscalate = session.isEscalated() || aiService.shouldEscalate(request.getMessage(), sentiment);
        boolean complaintRequest = isComplaintRegistrationRequest(request.getMessage());
        Long escalatedTicketId = session.getAssociatedTicketId();

        if (complaintRequest && !session.isEscalated()) {
            String ticketTitle = "Complaint: " + (request.getMessage().length() > 70
                    ? request.getMessage().substring(0, 67) + "..."
                    : request.getMessage());

            String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "customer@supportai.com";
            TicketRequest complaintRequestData = new TicketRequest(
                    ticketTitle,
                    request.getMessage(),
                    null,
                    null
            );

            try {
                TicketResponse ticket = ticketService.createTicket(complaintRequestData, email);
                escalatedTicketId = ticket.getId();
                session.setAssociatedTicketId(escalatedTicketId);
                sessionRepository.save(session);
            } catch (Exception e) {
                // Keep the chat usable even if ticket creation fails.
                escalatedTicketId = null;
            }
        } else if (shouldEscalate && !session.isEscalated()) {
            session.setEscalated(true);

            // Auto-create escalated ticket in background for agent handoff
            String ticketTitle = "Live Chat Escalation: " + (request.getMessage().length() > 60
                    ? request.getMessage().substring(0, 57) + "..."
                    : request.getMessage());

            String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "customer@supportai.com";
            TicketRequest tReq = new TicketRequest(ticketTitle, "Escalated from live chat session: " + sessionId + "\n\nUser: " + request.getMessage(), null, TicketPriority.URGENT);
            try {
                TicketResponse ticket = ticketService.createTicket(tReq, email);
                escalatedTicketId = ticket.getId();
                session.setAssociatedTicketId(escalatedTicketId);
            } catch (Exception ignored) {
            }

            sessionRepository.save(session);
        }

        // 5. Generate AI Response
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        String botReplyText;

        if (complaintRequest && escalatedTicketId != null) {
            botReplyText = "Absolutely. I have registered your complaint successfully. Your support ticket number is #"
                    + getTicketNumber(escalatedTicketId, userEmail)
                    + ". Our support team can now review the issue and follow up with you.";
        } else if (complaintRequest) {
            botReplyText = "I can register this as a support complaint. The ticket service is temporarily unavailable, so please try again in a moment or use the Human Agent option.";
        } else {
            botReplyText = aiService.generateChatbotResponse(
                    request.getMessage(),
                    history,
                    relevantFaqs,
                    session.isEscalated()
            );
        }

        // 6. Save AI message
        ChatMessage botMsg = new ChatMessage(
                sessionId,
                SenderType.AI_BOT,
                botReplyText,
                SentimentType.POSITIVE
        );
        messageRepository.save(botMsg);

        ChatMessageResponse response = new ChatMessageResponse(
                sessionId,
                SenderType.AI_BOT,
                botReplyText,
                sentiment.getLabel(),
                session.isEscalated(),
                relevantFaqs.size() > 2 ? relevantFaqs.subList(0, 2) : relevantFaqs
        );
        response.setEscalatedTicketId(escalatedTicketId);

        return response;
    }

    private boolean isComplaintRegistrationRequest(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String lower = message.toLowerCase();
        return lower.contains("register a complaint")
                || lower.contains("register complaint")
                || lower.contains("raise a complaint")
                || lower.contains("raise complaint")
                || lower.contains("file a complaint")
                || lower.contains("file complaint")
                || lower.contains("create a complaint")
                || lower.contains("create complaint")
                || lower.contains("open a complaint")
                || lower.contains("register a ticket")
                || lower.contains("create a ticket")
                || lower.contains("raise a ticket")
                || lower.contains("open a ticket");
    }

    private String getTicketNumber(Long ticketId, String userEmail) {
        try {
            String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "customer@supportai.com";
            return ticketService.getTicketById(ticketId, email).getTicketNumber();
        } catch (Exception e) {
            return String.valueOf(ticketId);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getSessionHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public TicketResponse escalateChatToTicket(ChatEscalateRequest request, String userEmail) {
        ChatSession session = sessionRepository.findBySessionId(request.getSessionId())
                .orElseGet(() -> sessionRepository.save(new ChatSession(request.getSessionId(), null, request.getCustomerName())));

        session.setEscalated(true);

        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(request.getSessionId());
        StringBuilder chatTranscript = new StringBuilder("Chat Transcript:\n");
        for (ChatMessage msg : history) {
            chatTranscript.append("[").append(msg.getSenderType()).append("]: ").append(msg.getContent()).append("\n");
        }

        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : "Escalated Chat Support Request";

        String description = (request.getReason() != null ? "Reason: " + request.getReason() + "\n\n" : "") + chatTranscript;

        String email = (userEmail != null && !userEmail.isBlank()) ? userEmail : "customer@supportai.com";
        TicketRequest ticketReq = new TicketRequest(title, description, null, TicketPriority.URGENT);
        TicketResponse ticket = ticketService.createTicket(ticketReq, email);

        session.setAssociatedTicketId(ticket.getId());
        sessionRepository.save(session);

        return ticket;
    }
}
