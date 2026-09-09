package com.supportai.service;

import com.supportai.dto.FaqRequest;
import com.supportai.model.FaqArticle;
import com.supportai.model.TicketCategory;
import com.supportai.repository.FaqArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return faqRepository.searchArticles(query.trim());
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
