package com.supportai.ai;

import com.supportai.model.TicketPriority;

public class PriorityResult {
    private TicketPriority priority;
    private String reasoning;
    private boolean slaRisk;

    public PriorityResult() {}

    public PriorityResult(TicketPriority priority, String reasoning, boolean slaRisk) {
        this.priority = priority;
        this.reasoning = reasoning;
        this.slaRisk = slaRisk;
    }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public boolean isSlaRisk() { return slaRisk; }
    public void setSlaRisk(boolean slaRisk) { this.slaRisk = slaRisk; }
}
