package com.yesgrad.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class PropertiesConfig {
    
    private Brevo brevo;
    private YesGrad yesgrad;
    private Frontend frontend;
    private Security security;
    
    @Data
    public static class Frontend {
        private String url;
    }
    
    @Data
    public static class Security {
        private int passwordHistoryLimit;
        private int resetRateLimit;
        private int resetRateWindowHours;
    }
    
    @Data
    public static class Brevo {
        private String apiKey;
        private Sender sender;
    }

    @Data
    public static class Sender {
        private String email;
        private String name;
    }

    @Data
    public static class YesGrad {
        private String companyName;
        private String phoneNumber;
    }
}
