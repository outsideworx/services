package application.configuration.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class Properties {
    private final Map<String, Client> clients;
    private final Map<String, Service> services;

    @Data
    public static final class Client {
        private String caller;
        private String origin;
        private String token;
    }

    @Data
    public static final class Service {
        private String url;
    }
}