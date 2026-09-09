package com.supportai.repository;

import com.supportai.model.FaqArticle;
import com.supportai.model.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqArticleRepository extends JpaRepository<FaqArticle, Long> {
    List<FaqArticle> findByCategory(TicketCategory category);

    @Query("SELECT f FROM FaqArticle f WHERE " +
           "LOWER(f.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.tags) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FaqArticle> searchArticles(@Param("query") String query);
}
