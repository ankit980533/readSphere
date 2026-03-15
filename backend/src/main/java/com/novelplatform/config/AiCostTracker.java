package com.novelplatform.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks OpenAI API usage and estimated costs
 */
@Aspect
@Component
public class AiCostTracker {
    
    private static final Logger log = LoggerFactory.getLogger(AiCostTracker.class);
    
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicLong totalInputTokens = new AtomicLong(0);
    private final AtomicLong totalOutputTokens = new AtomicLong(0);
    
    // GPT-3.5-turbo pricing
    private static final double INPUT_COST_PER_1M = 0.50;
    private static final double OUTPUT_COST_PER_1M = 1.50;
    
    @Around("execution(* com.novelplatform.service.AiService.call*(..))")
    public Object trackOpenAICost(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        
        // Estimate tokens based on method
        String methodName = joinPoint.getSignature().getName();
        TokenEstimate estimate = estimateTokens(methodName);
        
        totalCalls.incrementAndGet();
        totalInputTokens.addAndGet(estimate.input);
        totalOutputTokens.addAndGet(estimate.output);
        
        logCostSummary(methodName, estimate, duration);
        
        return result;
    }
    
    @Around("execution(* com.novelplatform.service.OllamaAiService.*(..))")
    public Object trackOllamaCost(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        
        totalCalls.incrementAndGet();
        log.info("Ollama call: {} - Duration: {}ms - Cost: $0.00 (FREE)", 
                joinPoint.getSignature().getName(), duration);
        
        return result;
    }
    
    private TokenEstimate estimateTokens(String methodName) {
        switch (methodName) {
            case "detectChapters":
                return new TokenEstimate(10000, 500);
            case "detectGenre":
                return new TokenEstimate(1500, 50);
            case "generateSummary":
                return new TokenEstimate(6000, 150);
            case "moderateContent":
                return new TokenEstimate(4000, 100);
            default:
                return new TokenEstimate(2000, 100);
        }
    }
    
    private void logCostSummary(String method, TokenEstimate estimate, long duration) {
        double callCost = calculateCost(estimate.input, estimate.output);
        double totalCost = calculateCost(totalInputTokens.get(), totalOutputTokens.get());
        
        log.info("AI Call: {} | Tokens: {}in/{}out | Cost: ${} | Duration: {}ms", 
                method, estimate.input, estimate.output, 
                String.format("%.4f", callCost), duration);
        
        log.info("Total AI Usage: {} calls | {} input tokens | {} output tokens | Total Cost: ${}",
                totalCalls.get(), totalInputTokens.get(), totalOutputTokens.get(),
                String.format("%.4f", totalCost));
    }
    
    private double calculateCost(long inputTokens, long outputTokens) {
        double inputCost = (inputTokens / 1_000_000.0) * INPUT_COST_PER_1M;
        double outputCost = (outputTokens / 1_000_000.0) * OUTPUT_COST_PER_1M;
        return inputCost + outputCost;
    }
    
    public CostSummary getCostSummary() {
        return new CostSummary(
            totalCalls.get(),
            totalInputTokens.get(),
            totalOutputTokens.get(),
            calculateCost(totalInputTokens.get(), totalOutputTokens.get())
        );
    }
    
    private static class TokenEstimate {
        final long input;
        final long output;
        
        TokenEstimate(long input, long output) {
            this.input = input;
            this.output = output;
        }
    }
    
    public static class CostSummary {
        public final int totalCalls;
        public final long inputTokens;
        public final long outputTokens;
        public final double totalCost;
        
        public CostSummary(int totalCalls, long inputTokens, long outputTokens, double totalCost) {
            this.totalCalls = totalCalls;
            this.inputTokens = inputTokens;
            this.outputTokens = outputTokens;
            this.totalCost = totalCost;
        }
    }
}
