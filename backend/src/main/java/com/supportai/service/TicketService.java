package com.supportai.service;

import com.supportai.ai.AIService;
import com.supportai.ai.ClassificationResult;
import com.supportai.ai.PriorityResult;
import com.supportai.ai.SentimentResult;
import com.supportai.dto.TicketMessageDto;
import com.supportai.dto.TicketMessageRequest;
import com.supportai.dto.TicketRequest;
import com.supportai.dto.TicketResponse;
import com.supportai.model.*;
import com.supportai.repository.TicketMessageRepository;
import com.supportai.repository.TicketRepository;
import com.supportai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketMessageRepository ticketMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private FaqService faqService;

    @Transactional
    public TicketResponse createTicket(TicketRequest request, String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // 1. AI Sentiment Analysis
        SentimentResult sentiment = aiService.analyzeSentiment(request.getTitle() + " " + request.getDescription());

        // 2. AI Ticket Classification
        TicketCategory category = request.getCategory();
        if (category == null) {
            ClassificationResult classification = aiService.classifyTicket(request.getTitle(), request.getDescription());
            category = classification.getCategory();
        }

        // 3. AI Priority Determination
        TicketPriority priority = request.getPriority();
        if (priority == null) {
            PriorityResult priorityResult = aiService.determinePriority(request.getTitle(), request.getDescription(), sentiment);
            priority = priorityResult.getPriority();
        }

        // Generate unique ticket number: TCK-YYYYMMDD-XXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String ticketNumber = "TCK-" + datePart + "-" + randomPart;

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setCustomer(customer);
        ticket.setSentimentScore(sentiment.getScore());
        ticket.setSentimentLabel(sentiment.getLabel());

        if (sentiment.isEscalationTriggered() || priority == TicketPriority.URGENT) {
            ticket.setStatus(TicketStatus.ESCALATED);
            ticket.setEscalatedAt(LocalDateTime.now());
        } else {
            ticket.setStatus(TicketStatus.OPEN);
        }

        ticket = ticketRepository.save(ticket);

        // Add initial customer message to thread
        TicketMessage customerMsg = new TicketMessage(
                ticket,
                customer.getId(),
                SenderType.CUSTOMER,
                customer.getFullName(),
                request.getDescription(),
                sentiment.getLabel()
        );
        ticketMessageRepository.save(customerMsg);

        // Generate initial AI automated acknowledgement / suggested solution
        List<FaqArticle> relevantFaqs = faqService.searchFaqs(request.getTitle());
        String aiReplyText = aiService.generateChatbotResponse(
                request.getDescription(),
                List.of(),
                relevantFaqs,
                ticket.getStatus() == TicketStatus.ESCALATED
        );

        TicketMessage aiMsg = new TicketMessage(
                ticket,
                null,
                SenderType.AI_BOT,
                "SupportAI Assistant",
                aiReplyText,
                SentimentType.POSITIVE
        );
        ticketMessageRepository.save(aiMsg);

        return getTicketById(ticket.getId(), userEmail);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<Ticket> tickets;
        if (user.getRole() == Role.ROLE_AGENT || user.getRole() == Role.ROLE_ADMIN) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tickets = ticketRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId());
        }

        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id, String userEmail) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));

        TicketResponse response = mapToResponse(ticket);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(id);
        response.setMessages(messages.stream().map(TicketMessageDto::fromEntity).collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public TicketResponse updateStatus(Long id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));

        ticket.setStatus(status);
        if (status == TicketStatus.ESCALATED && ticket.getEscalatedAt() == null) {
            ticket.setEscalatedAt(LocalDateTime.now());
        }
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse assignAgent(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + ticketId));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with id: " + agentId));

        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.ESCALATED) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse addMessage(Long ticketId, TicketMessageRequest request, String senderEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + ticketId));

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderEmail));

        SenderType senderType = (sender.getRole() == Role.ROLE_AGENT || sender.getRole() == Role.ROLE_ADMIN)
                ? SenderType.AGENT
                : SenderType.CUSTOMER;

        SentimentResult sentiment = aiService.analyzeSentiment(request.getMessage());

        TicketMessage msg = new TicketMessage(
                ticket,
                sender.getId(),
                senderType,
                sender.getFullName(),
                request.getMessage(),
                sentiment.getLabel()
        );
        ticketMessageRepository.save(msg);

        // Update overall ticket sentiment if customer responded with negative sentiment
        if (senderType == SenderType.CUSTOMER) {
            ticket.setSentimentScore(sentiment.getScore());
            ticket.setSentimentLabel(sentiment.getLabel());

            // Check if escalation triggered
            if (aiService.shouldEscalate(request.getMessage(), sentiment)) {
                ticket.setStatus(TicketStatus.ESCALATED);
                ticket.setPriority(TicketPriority.URGENT);
                if (ticket.getEscalatedAt() == null) {
                    ticket.setEscalatedAt(LocalDateTime.now());
                }
            } else if (ticket.getStatus() == TicketStatus.RESOLVED) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }
        }

        ticket = ticketRepository.save(ticket);
        return getTicketById(ticketId, senderEmail);
    }

    @Transactional
    public TicketResponse escalateTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + ticketId));

        ticket.setStatus(TicketStatus.ESCALATED);
        ticket.setPriority(TicketPriority.URGENT);
        ticket.setEscalatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);

        TicketMessage systemMsg = new TicketMessage(
                ticket,
                null,
                SenderType.SYSTEM,
                "System",
                "Ticket has been escalated to Tier-2 Human Agent Support queue.",
                SentimentType.NEUTRAL
        );
        ticketMessageRepository.save(systemMsg);

        return mapToResponse(ticket);
    }

    @Transactional(readOnly = true)
    public String getAiSuggestedReply(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + ticketId));

        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        String lastCustomerMsg = messages.stream()
                .filter(m -> m.getSenderType() == SenderType.CUSTOMER)
                .reduce((first, second) -> second)
                .map(TicketMessage::getMessage)
                .orElse(ticket.getDescription());

        String customerName = ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : "Customer";

        return aiService.generateAgentDraftReply(
                customerName,
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getSentimentLabel(),
                lastCustomerMsg
        );
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse res = TicketResponse.fromEntity(ticket);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        res.setMessages(messages.stream().map(TicketMessageDto::fromEntity).collect(Collectors.toList()));
        return res;
    }
}
