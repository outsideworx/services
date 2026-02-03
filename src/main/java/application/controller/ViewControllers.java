package application.controller;

import application.configuration.utils.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
class ViewControllers implements WebMvcConfigurer {
    private final Properties properties;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        properties
                .getClients()
                .values()
                .forEach(client -> registry.addRedirectViewController(
                        "clients/".concat(client.getCaller()),
                        client.getOrigin()
                ));
        properties
                .getServices()
                .forEach((name, service) -> registry.addRedirectViewController(
                        "/".concat(name),
                        service.getUrl()
                ));
    }
}