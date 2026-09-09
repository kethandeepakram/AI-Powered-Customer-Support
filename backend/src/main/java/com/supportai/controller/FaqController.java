package com.supportai.controller;

import com.supportai.dto.FaqRequest;
import com.supportai.model.FaqArticle;
import com.supportai.model.TicketCategory;
import com.supportai.service.FaqService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faq")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FaqController {

    @Autowired
    private FaqService faqService;

    @GetMapping
    public ResponseEntity<List<FaqArticle>> getAllFaqs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TicketCategory category) {

        if (category != null) {
            return ResponseEntity.ok(faqService.getFaqsByCategory(category));
        }

        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(faqService.searchFaqs(q));
        }

        return ResponseEntity.ok(faqService.getAllFaqs());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_AGENT', 'ROLE_ADMIN')")
    public ResponseEntity<FaqArticle> createFaq(@Valid @RequestBody FaqRequest request) {
        FaqArticle article = faqService.createFaq(request);
        return ResponseEntity.ok(article);
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<FaqArticle> markHelpful(@PathVariable Long id) {
        FaqArticle article = faqService.markHelpful(id);
        return ResponseEntity.ok(article);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<FaqArticle> incrementView(@PathVariable Long id) {
        FaqArticle article = faqService.incrementView(id);
        return ResponseEntity.ok(article);
    }
}
