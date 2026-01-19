package application.controller;

import application.model.LicenseEntity;
import application.repository.LicenseRepository;
import application.repository.UserRepository;
import application.service.GrafanaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("${app.clients.ciafo.origin}, ${app.clients.peeps.origin}, ${app.clients.soup.origin}")
@RestController
@RequiredArgsConstructor
@Slf4j
final class LicenseController {
    private final GrafanaService grafanaService;

    private final LicenseRepository licenseRepository;

    private final UserRepository userRepository;

    @PostMapping("/api/licenses")
    void approve(@RequestHeader("X-Caller-Id") String callerId, @RequestParam String version) {
        grafanaService.registerRequest("licenses", "all");
        log.info("License approval request received for: [{}] with version: [{}]", callerId, version);
        // TODO: FIXME!
    }

    @GetMapping("/api/licenses")
    LicenseEntity licenses() {
        // TODO: FIXME!
        return null;
    }
}