package com.novelplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class OpenAiConfig {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAiConfig.class);
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${ai.enabled:true}")
    private boolean aiEnabled;
    
    @Value("${ai.provider:openai}")
    private String aiProvider;
    
    @PostConstruct
    public void validateConfiguration() {
        if (!aiEnabled) {
            log.warn("⚠️  AI features are DISABLED. Set ai.enabled=true to enable.");
            return;
        }
        
        if (!"openai".equals(aiProvider)) {
            log.info("ℹ️  AI provider is set to: {}", aiProvider);
            return;
        }
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-openai-api-key")) {
            log.error("❌ OpenAI API key is NOT configured!");
            log.error("   Please set OPENAI_API_KEY environment variable");
            log.error("   Get your key from: https://platform.openai.com/api-keys");
            log.error("   Example: export OPENAI_API_KEY=\"sk-your-key-here\"");
            throw new IllegalStateException("OpenAI API key is required when ai.provider=openai");
        }
        
        if (!apiKey.startsWith("sk-")) {
            log.error("❌ Invalid OpenAI API key format!");
            log.error("   API keys should start with 'sk-'");
            log.error("   Current value: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            throw new IllegalStateException("Invalid OpenAI API key format");
        }
        
        log.info("✅ OpenAI API configured successfully");
        log.info("   Provider: OpenAI GPT-3.5-turbo");
        log.info("   API Key: {}...{}", 
                apiKey.substring(0, 7), 
                apiKey.substring(apiKey.length() - 4));
        log.info("   Free Credit: $5 (covers ~416 novels)");
        log.info("   Cost per novel: ~$0.012");
        log.info("   Monitor usage: https://platform.openai.com/usage");
    }
}
