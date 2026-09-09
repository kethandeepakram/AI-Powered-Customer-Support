package com.supportai.service;

import com.supportai.dto.AnalyticsSummaryDto;
import com.supportai.model.Ticket;
import com.supportai.model.TicketCategory;
import com.supportai.model.TicketPriority;
import com.supportai.model.TicketStatus;
import com.supportai.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private TicketRepository ticketRepository;

    public AnalyticsSummaryDto getAnalyticsSummary() {
        List<Ticket> allTickets = ticketRepository.findAll();
        AnalyticsSummaryDto dto = new AnalyticsSummaryDto();

        long total = allTickets.size();
        dto.setTotalTickets(total);

        long open = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long escalated = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.ESCALATED).count();
        long resolved = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED).count();

        dto.setOpenTickets(open);
        dto.setInProgressTickets(inProgress);
        dto.setEscalatedTickets(escalated);
        dto.setResolvedTickets(resolved);

        double resRate = total > 0 ? ((double) resolved / total) * 100.0 : 0.0;
        dto.setResolutionRate(Math.round(resRate * 10.0) / 10.0);

        double avgSentiment = allTickets.stream()
                .mapToDouble(t -> t.getSentimentScore() != null ? t.getSentimentScore() : 0.0)
                .average()
                .orElse(0.0);
        dto.setAverageSentimentScore(Math.round(avgSentiment * 100.0) / 100.0);

        Map<String, Long> categoryMap = new HashMap<>();
        for (TicketCategory cat : TicketCategory.values()) {
            long count = allTickets.stream().filter(t -> t.getCategory() == cat).count();
            categoryMap.put(cat.name(), count);
        }
        dto.setCategoryDistribution(categoryMap);

        Map<String, Long> priorityMap = new HashMap<>();
        for (TicketPriority pri : TicketPriority.values()) {
            long count = allTickets.stream().filter(t -> t.getPriority() == pri).count();
            priorityMap.put(pri.name(), count);
        }
        dto.setPriorityDistribution(priorityMap);

        return dto;
    }
}
