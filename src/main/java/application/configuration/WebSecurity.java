package application.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class WebSecurity {
    @Value("${spring.logout.url}")
    private String logoutUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/actuator/**",
                                "/api/**",
                                "/clients/**",
                                "/grafana",
                                "/img/**",
                                "/ntfy",
                                "/robots.txt",
                                "/sitemap.xml")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessUrl(logoutUrl))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}