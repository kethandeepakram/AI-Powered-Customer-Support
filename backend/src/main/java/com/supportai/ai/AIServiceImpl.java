package com.supportai.ai;

import com.supportai.model.ChatMessage;
import com.supportai.model.FaqArticle;
import com.supportai.model.SentimentType;
import com.supportai.model.TicketCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Autowired
    private LocalNLPEngine localNLPEngine;

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private OpenAIClient openAIClient;

    @Value("${ai.provider:auto}")
    private String provider;

    @Override
    public SentimentResult analyzeSentiment(String text) {
        // Fast, deterministic NLP sentiment analysis
        return localNLPEngine.analyzeSentiment(text);
    }

    @Override
    public ClassificationResult classifyTicket(String title, String description) {
        // If Gemini or OpenAI is active, we can refine or default to high-accuracy local NLP
        return localNLPEngine.classifyTicket(title, description);
    }

    @Override
    public PriorityResult determinePriority(String title, String description, SentimentResult sentiment) {
        return localNLPEngine.determinePriority(title, description, sentiment);
    }

    @Override
    public String generateChatbotResponse(String userMessage, List<ChatMessage> history, List<FaqArticle> faqs, boolean isEscalated) {
        if (isEscalated) {
            return localNLPEngine.generateChatbotResponse(userMessage, faqs, true);
        }

        // Check if user requested human escalation
        if (localNLPEngine.checkEscalationIntent(userMessage)) {
            return localNLPEngine.generateChatbotResponse(userMessage, faqs, false);
        }

        // Try Gemini / OpenAI if configured
        if ("gemini".equalsIgnoreCase(provider) || "auto".equalsIgnoreCase(provider)) {
            if (geminiClient.isConfigured()) {
                try {
                    String systemPrompt = buildSystemPrompt(faqs, history);
                    return geminiClient.generateContent(systemPrompt, userMessage);
                } catch (Exception e) {
                    logger.warn("Gemini generation failed, falling back to Local NLP: {}", e.getMessage());
                }
            }
        }

        if ("openai".equalsIgnoreCase(provider) || "auto".equalsIgnoreCase(provider)) {
            if (openAIClient.isConfigured()) {
                try {
                    String systemPrompt = buildSystemPrompt(faqs, history);
                    return openAIClient.generateCompletion(systemPrompt, userMessage);
                } catch (Exception e) {
                    logger.warn("OpenAI generation failed, falling back to Local NLP: {}", e.getMessage());
                }
            }
        }

        // Local NLP Engine Fallback
        return localNLPEngine.generateChatbotResponse(userMessage, faqs, false);
    }

    @Override
    public String generateAgentDraftReply(String customerName, String title, TicketCategory category,
                                          SentimentType sentiment, String lastCustomerMessage) {
        // Try LLM for agent draft reply
        String prompt = "Draft a helpful, professional customer support agent reply to " + customerName +
                " regarding ticket: \"" + title + "\" (Category: " + category + ", Customer Sentiment: " + sentiment + "). " +
                "Latest customer message: \"" + (lastCustomerMessage != null ? lastCustomerMessage : title) + "\".";

        if (geminiClient.isConfigured() && ("gemini".equalsIgnoreCase(provider) || "auto".equalsIgnoreCase(provider))) {
            try {
                return geminiClient.generateContent(
                        "You are an empathetic, expert customer support specialist. Keep the reply clear, actionable, and courteous.",
                        prompt);
            } catch (Exception e) {
                logger.warn("Gemini agent draft failed, using local template: {}", e.getMessage());
            }
        }

        if (openAIClient.isConfigured() && ("openai".equalsIgnoreCase(provider) || "auto".equalsIgnoreCase(provider))) {
            try {
                return openAIClient.generateCompletion(
                        "You are an empathetic, expert customer support specialist. Keep the reply clear, actionable, and courteous.",
                        prompt);
            } catch (Exception e) {
                logger.warn("OpenAI agent draft failed, using local template: {}", e.getMessage());
            }
        }

        return localNLPEngine.generateAgentDraftReply(customerName, title, category, sentiment);
    }

    @Override
    public boolean shouldEscalate(String text, SentimentResult sentiment) {
        if (sentiment != null && sentiment.isEscalationTriggered()) {
            return true;
        }
        return localNLPEngine.checkEscalationIntent(text);
    }

    private String buildSystemPrompt(List<FaqArticle> faqs, List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are SupportAI, an intelligent, empathetic customer support assistant for our software platform.\n");
        sb.append("Be natural and conversational. Answer general questions directly instead of assuming every message is a support incident.\n");
        sb.append("Be polite, concise, and helpful. If the customer expresses severe anger or requests a human, offer to escalate immediately.\n");
        sb.append("Use the recent conversation history to maintain context. Do not repeat questions the customer has already answered.\n");

        if (history != null && !history.isEmpty()) {
            sb.append("\nRecent conversation history:\n");
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                ChatMessage message = history.get(i);
                String speaker = message.getSenderType() == null
                        ? "Unknown"
                        : message.getSenderType().name();
                sb.append("- ").append(speaker).append(": ")
                        .append(message.getContent()).append("\n");
            }
        }
        if (faqs != null && !faqs.isEmpty()) {
            sb.append("\nRelevant Knowledge-Base Articles for grounding:\n");
            for (FaqArticle faq : faqs) {
                sb.append("- Title: ").append(faq.getTitle()).append("\n");
                sb.append("  Content: ").append(faq.getContent()).append("\n");
            }
        }
        return sb.toString();
    }
}
