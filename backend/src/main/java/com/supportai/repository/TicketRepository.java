package com.supportai.repository;

import com.supportai.model.Ticket;
import com.supportai.model.TicketCategory;
import com.supportai.model.TicketPriority;
import com.supportai.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByStatusNot(TicketStatus status);
    List<Ticket> findByAssignedAgentId(Long agentId);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    long countByStatus(TicketStatus status);
    long countByCategory(TicketCategory category);
    long countByPriority(TicketPriority priority);
}
