package com.supportai.ai;

import com.supportai.model.FaqArticle;
import com.supportai.model.SentimentType;
import com.supportai.model.TicketCategory;
import com.supportai.model.TicketPriority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class LocalNLPEngine {

    private static final Set<String> FRUSTRATED_WORDS = Set.of(
            "furious", "angry", "terrible", "horrible", "unacceptable", "scam", "worst",
            "charged twice", "refund immediately", "cancel immediately", "lawyer", "sue",
            "ridiculous", "incompetent", "disaster", "broken completely", "stealing", "rip-off"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "bad", "broken", "bug", "error", "fail", "failed", "failing", "fault", "issue",
            "not working", "cannot", "cant", "doesn't work", "does not work", "slow", "down",
            "crash", "crashed", "stuck", "trouble", "problem", "glitch", "wrong", "lost", "missing"
    );

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "good", "great", "excellent", "awesome", "resolved", "solved", "fixed", "thank",
            "thanks", "appreciate", "helpful", "perfect", "fantastic", "love", "works well", "pleased"
    );

    private static final Set<String> ESCALATION_PHRASES = Set.of(
            "talk to human", "speak to human", "real person", "human agent", "human representative",
            "connect to agent", "agent please", "talk to representative", "customer service agent",
            "supervisor", "manager", "escalate this", "escalate", "speak with someone"
    );

    private static final Map<TicketCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            TicketCategory.BILLING, List.of("invoice", "payment", "card", "charge", "charged", "billing", "refund", "subscription", "price", "receipt", "plan", "cost", "dollar", "credit card", "bank"),
            TicketCategory.ACCOUNT_ACCESS, List.of("password", "login", "log in", "sign in", "signin", "2fa", "two-factor", "mfa", "reset password", "locked", "account", "email verification", "otp", "auth", "session"),
            TicketCategory.TECHNICAL_SUPPORT, List.of("api", "webhook", "error", "bug", "crash", "500", "404", "exception", "integration", "sdk", "database", "timeout", "slow", "down", "server", "code", "latency"),
            TicketCategory.FEATURE_REQUEST, List.of("feature", "request", "add", "suggest", "enhancement", "would like", "can you add", "support for", "hope you add", "roadmap", "proposal"),
            TicketCategory.GENERAL_INQUIRY, List.of("how to", "what is", "where is", "help", "information", "question", "pricing", "guide", "documentation", "hours", "contact")
    );

    public SentimentResult analyzeSentiment(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentResult(SentimentType.NEUTRAL, 0.0, Collections.emptyList(), false);
        }

        String lower = text.toLowerCase();
        List<String> matchedKeywords = new ArrayList<>();
        double score = 0.0;

        for (String phrase : FRUSTRATED_WORDS) {
            if (lower.contains(phrase)) {
                matchedKeywords.add(phrase);
                score -= 0.45;
            }
        }

        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) {
                matchedKeywords.add(word);
                score -= 0.15;
            }
        }

        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) {
                matchedKeywords.add(word);
                score += 0.25;
            }
        }

        // Clamp between -1.0 and 1.0
        score = Math.max(-1.0, Math.min(1.0, score));

        SentimentType label;
        boolean escalationTriggered = false;

        if (score <= -0.5 || matchedKeywords.stream().anyMatch(FRUSTRATED_WORDS::contains)) {
            label = SentimentType.FRUSTRATED;
            escalationTriggered = true;
        } else if (score < -0.1) {
            label = SentimentType.NEGATIVE;
        } else if (score > 0.15) {
            label = SentimentType.POSITIVE;
        } else {
            label = SentimentType.NEUTRAL;
        }

        // Check explicit escalation request
        if (checkEscalationIntent(lower)) {
            escalationTriggered = true;
        }

        return new SentimentResult(label, Math.round(score * 100.0) / 100.0, matchedKeywords, escalationTriggered);
    }

    public boolean checkEscalationIntent(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String phrase : ESCALATION_PHRASES) {
            if (lower.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    public ClassificationResult classifyTicket(String title, String description) {
        String combined = (title + " " + (description != null ? description : "")).toLowerCase();

        TicketCategory bestCategory = TicketCategory.GENERAL_INQUIRY;
        int maxHits = 0;
        List<String> matchedTags = new ArrayList<>();

        for (Map.Entry<TicketCategory, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int hits = 0;
            List<String> tagsForCat = new ArrayList<>();
            for (String kw : entry.getValue()) {
                if (combined.contains(kw)) {
                    hits++;
                    tagsForCat.add(kw);
                }
            }
            if (hits > maxHits) {
                maxHits = hits;
                bestCategory = entry.getKey();
                matchedTags = tagsForCat;
            }
        }

        double confidence = maxHits == 0 ? 0.50 : Math.min(0.98, 0.60 + (maxHits * 0.10));
        String reasoning = "Classified as " + bestCategory + " based on keywords: " + String.join(", ", matchedTags);

        return new ClassificationResult(bestCategory, Math.round(confidence * 100.0) / 100.0, matchedTags, reasoning);
    }

    public PriorityResult determinePriority(String title, String description, SentimentResult sentiment) {
        String combined = (title + " " + (description != null ? description : "")).toLowerCase();

        boolean urgentKeywords = combined.contains("production down") || combined.contains("outage") ||
                combined.contains("data loss") || combined.contains("security") || combined.contains("charged twice") ||
                combined.contains("hacked") || combined.contains("breach");

        boolean highKeywords = combined.contains("cannot login") || combined.contains("refund") ||
                combined.contains("blocked") || combined.contains("urgent") || combined.contains("payment failed") ||
                combined.contains("critical");

        if (urgentKeywords || sentiment.getLabel() == SentimentType.FRUSTRATED) {
            return new PriorityResult(TicketPriority.URGENT, "Critical keywords or high customer frustration detected.", true);
        }

        if (highKeywords || sentiment.getLabel() == SentimentType.NEGATIVE) {
            return new PriorityResult(TicketPriority.HIGH, "High-impact issue or negative sentiment detected.", true);
        }

        if (combined.contains("how to") || combined.contains("feature") || combined.contains("enhancement")) {
            return new PriorityResult(TicketPriority.LOW, "Routine inquiry or feature request.", false);
        }

        return new PriorityResult(TicketPriority.MEDIUM, "Standard operational support inquiry.", false);
    }

    public String generateChatbotResponse(String message, List<FaqArticle> faqs, boolean isEscalated) {
        if (isEscalated) {
            return "I have escalated your request to our priority human support queue. A live specialist is reviewing your context and will be with you shortly. Thank you for your patience!";
        }

        String lower = message.toLowerCase().trim();

        if (checkEscalationIntent(lower)) {
            return "I completely understand. I am transferring you directly to a human agent right now. An agent will review our conversation history so you don't have to repeat anything.";
        }

        if (lower.matches("^(hi|hello|hey|good morning|good afternoon|greetings).*")) {
            return "Hello! I'm SupportAI, your virtual assistant. How can I help you today? You can ask about billing, account security, API integration, or type 'speak to agent' if you prefer human support.";
        }

        if (!faqs.isEmpty()) {
            FaqArticle topFaq = faqs.get(0);
            return "Here is information regarding your query:\n\n**" + topFaq.getTitle() + "**\n" +
                    topFaq.getContent() + "\n\nDid this resolve your question? If not, reply with more details or ask for a live human agent.";
        }

        return "Thank you for reaching out. I've noted your question: \"" + message + "\".\n\n" +
                "To assist you best: could you provide any error messages, transaction IDs, or specific steps to reproduce the issue? Alternatively, say 'agent' at any time to connect with a representative.";
    }

    public String generateAgentDraftReply(String customerName, String title, TicketCategory category, SentimentType sentiment) {
        String greeting = "Hello " + (customerName != null ? customerName : "Customer") + ",\n\n";
        String empathy = "";

        if (sentiment == SentimentType.FRUSTRATED || sentiment == SentimentType.NEGATIVE) {
            empathy = "Thank you for contacting us, and I sincerely apologize for the frustration and inconvenience this issue has caused.\n\n";
        } else {
            empathy = "Thank you for contacting SupportAI support.\n\n";
        }

        String body;
        switch (category) {
            case BILLING:
                body = "I have reviewed your billing record regarding \"" + title + "\". Our finance operations team has flagged this transaction for priority audit. If any duplicate or erroneous charges occurred, they will be refunded back to your original payment method within 3-5 business days.";
                break;
            case ACCOUNT_ACCESS:
                body = "Regarding your access issue with \"" + title + "\", I have verified your account status and sent a secure one-time password verification link to your primary email address. Please follow the instructions to safely unlock your profile.";
                break;
            case TECHNICAL_SUPPORT:
                body = "Our engineering tier-2 team is actively investigating the technical error reported in \"" + title + "\". We are inspecting our system logs and telemetry for any anomalous behavior and will update you with a hotfix or workaround shortly.";
                break;
            default:
                body = "I am currently reviewing your inquiry regarding \"" + title + "\". I will gather the required details and provide you with a comprehensive update shortly.";
        }

        String closing = "\n\nPlease let me know if you have any additional questions or need immediate assistance.\n\nBest regards,\nSupportAI Support Team";

        return greeting + empathy + body + closing;
    }
}
