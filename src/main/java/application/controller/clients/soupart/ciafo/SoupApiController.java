package application.controller.clients.soupart.ciafo;

import application.model.clients.soup.mapping.SoupImage;
import application.repository.clients.SoupRepository;
import application.service.GrafanaService;
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
    private final GrafanaService grafanaService;

    private final SoupRepository soupRepository;

    @GetMapping("/api/cached/soupart")
    List<SoupImage> getSoupImages(@RequestParam String category, @RequestParam int offset) {
        log.info("Incoming API request for category: [{}], with offset: [{}]", category, offset);
        grafanaService.registerRequest("soupart", "all");
        return soupRepository.getImagesByCategoryAndOffset(category, offset);
    }
}