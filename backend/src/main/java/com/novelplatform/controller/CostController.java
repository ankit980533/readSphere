package com.novelplatform.controller;

import com.novelplatform.config.AiCostTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Monitor AI usage and costs
 */
@RestController
@RequestMapping("/api/admin/costs")
@PreAuthorize("hasRole('ADMIN')")
public class CostController {
    
    @Autowired
    private AiCostTracker costTracker;
    
    @GetMapping("/summary")
    public AiCostTracker.CostSummary getCostSummary() {
        return costTracker.getCostSummary();
    }
}
