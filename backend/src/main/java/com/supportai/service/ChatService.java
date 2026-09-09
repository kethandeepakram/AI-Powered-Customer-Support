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

        // 4. Check for Escalation
        boolean shouldEscalate = session.isEscalated() || aiService.shouldEscalate(request.getMessage(), sentiment);
        Long escalatedTicketId = session.getAssociatedTicketId();

        if (shouldEscalate && !session.isEscalated()) {
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
        String botReplyText = aiService.generateChatbotResponse(
                request.getMessage(),
                history,
                relevantFaqs,
                session.isEscalated()
        );

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
