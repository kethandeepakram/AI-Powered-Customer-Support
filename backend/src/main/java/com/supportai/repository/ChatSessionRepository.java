package com.supportai.repository;

import com.supportai.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionId(String sessionId);
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
