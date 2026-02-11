package application.controller.clients.ciafo;

import application.model.clients.ciafo.mapping.CiafoFirstImage;
import application.model.clients.ciafo.mapping.CiafoImages;
import application.repository.clients.CiafoRepository;
import application.service.GrafanaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("${app.clients.ciafo.origin}")
@RestController
@RequiredArgsConstructor
@Slf4j
final class CiafoApiController {
    private final CiafoRepository ciafoRepository;

    private final GrafanaService grafanaService;

    @GetMapping("/api/come-in-and-find-out")
    List<CiafoFirstImage> getCiafoFirstImages(@RequestParam String category, @RequestParam int offset) {
        log.info("Incoming API request for category: [{}], with offset: [{}]", category, offset);
        grafanaService.registerRequest("come-in-and-find-out", category);
        return ciafoRepository.getFirstImagesByCategoryAndOffset(category, offset);
    }

    @GetMapping("/api/cached/come-in-and-find-out")
    CiafoImages getCiafoImages(@RequestParam Long id) {
        log.info("Incoming API request for ID: [{}]", id);
        grafanaService.registerRequest("come-in-and-find-out", "details");
        return ciafoRepository.getImagesById(id);
    }
}