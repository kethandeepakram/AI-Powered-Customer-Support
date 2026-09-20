package com.supportai.service;

import com.supportai.dto.FaqRequest;
import com.supportai.model.FaqArticle;
import com.supportai.model.TicketCategory;
import com.supportai.repository.FaqArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FaqService {

    @Autowired
    private FaqArticleRepository faqRepository;

    public List<FaqArticle> getAllFaqs() {
        return faqRepository.findAll();
    }

    public List<FaqArticle> getFaqsByCategory(TicketCategory category) {
        return faqRepository.findByCategory(category);
    }

    public List<FaqArticle> searchFaqs(String query) {
        if (query == null || query.isBlank()) {
            return faqRepository.findAll();
        }

        String normalized = query.toLowerCase(Locale.ROOT).trim();

        // First try the database's exact substring search.
        List<FaqArticle> directMatches = faqRepository.searchArticles(normalized);
        if (!directMatches.isEmpty()) {
            return directMatches;
        }

        // If the user asks a natural-language question, the whole sentence
        // usually will not match an FAQ title/content exactly. Rank FAQs by
        // meaningful words instead.
        Set<String> stopWords = Set.of(
                "the", "a", "an", "and", "or", "to", "for", "of", "in", "on",
                "is", "are", "do", "does", "how", "what", "why", "where", "can",
                "could", "would", "i", "me", "my", "we", "our", "you", "your",
                "please", "tell", "about", "with", "from", "this", "that"
        );

        List<String> words = Arrays.stream(normalized.split("[^a-z0-9]+"))
                .filter(w -> w.length() >= 3 && !stopWords.contains(w))
                .distinct()
                .toList();

        if (words.isEmpty()) {
            return Collections.emptyList();
        }

        return faqRepository.findAll().stream()
                .map(faq -> {
                    String text = ((faq.getTitle() == null ? "" : faq.getTitle()) + " " +
                            (faq.getContent() == null ? "" : faq.getContent()) + " " +
                            (faq.getTags() == null ? "" : faq.getTags()))
                            .toLowerCase(Locale.ROOT);

                    int score = 0;
                    for (String word : words) {
                        if (text.contains(word)) {
                            score++;
                        }
                    }

                    // Exact title word matches are more useful than body-only matches.
                    String title = faq.getTitle() == null ? "" : faq.getTitle().toLowerCase(Locale.ROOT);
                    for (String word : words) {
                        if (title.contains(word)) {
                            score++;
                        }
                    }

                    return new AbstractMap.SimpleEntry<>(faq, score);
                })
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public FaqArticle createFaq(FaqRequest request) {
        FaqArticle article = new FaqArticle(
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getTags()
        );
        return faqRepository.save(article);
    }

    public FaqArticle markHelpful(Long id) {
        FaqArticle article = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ article not found with id: " + id));
        article.setHelpfulCount(article.getHelpfulCount() + 1);
        return faqRepository.save(article);
    }

    public FaqArticle incrementView(Long id) {
        FaqArticle article = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ article not found with id: " + id));
        article.setViewCount(article.getViewCount() + 1);
        return faqRepository.save(article);
    }
}
