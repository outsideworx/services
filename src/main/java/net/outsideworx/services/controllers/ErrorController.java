package net.outsideworx.services.controllers;

import lombok.extern.slf4j.Slf4j;
import net.outsideworx.services.configuration.utils.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
final class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    private final Properties properties;

    @GetMapping("/error")
    String error() {
        log.error("Supressing Whitelabel Error Page");
        return "redirect:".concat(properties.getServices().get("oauth").getUrl());
    }
}