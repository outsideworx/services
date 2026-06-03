package net.outsideworx.services.controllers.clients.soupart.ciafo;

import net.outsideworx.services.models.clients.soup.SoupEntity;
import net.outsideworx.services.repositories.clients.SoupRepository;
import net.outsideworx.services.gateways.GrafanaGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("${app.clients.soup.origin}")
@RestController
@RequiredArgsConstructor
@Slf4j
final class SoupApiController {
    private final GrafanaGateway grafanaGateway;

    private final SoupRepository soupRepository;

    @GetMapping("/api/cache/soupart")
    List<SoupEntity> getSoupItems(@RequestParam String category, @RequestParam int offset) {
        log.info("Incoming API request for category: [{}], with offset: [{}]", category, offset);
        grafanaGateway.registerRequest("soupart", category);
        return soupRepository.get(category, offset);
    }
}