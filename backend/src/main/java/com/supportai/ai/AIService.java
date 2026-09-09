package com.supportai.ai;

import com.supportai.model.ChatMessage;
import com.supportai.model.FaqArticle;
import com.supportai.model.SentimentType;
import com.supportai.model.TicketCategory;

import java.util.List;

public interface AIService {
    SentimentResult analyzeSentiment(String text);
    ClassificationResult classifyTicket(String title, String description);
    PriorityResult determinePriority(String title, String description, SentimentResult sentiment);
    String generateChatbotResponse(String userMessage, List<ChatMessage> history, List<FaqArticle> faqs, boolean isEscalated);
    String generateAgentDraftReply(String customerName, String title, TicketCategory category, SentimentType sentiment, String lastCustomerMessage);
    boolean shouldEscalate(String text, SentimentResult sentiment);
}
