package application.controller.clients.peeps;

import application.model.clients.peeps.PeepsEntity;
import application.repository.clients.PeepsRepository;
import application.service.GrafanaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("${app.clients.peeps.origin}")
@RestController
@RequiredArgsConstructor
@Slf4j
final class PeepsApiController {
    private final GrafanaService grafanaService;

    private final PeepsRepository peepsRepository;

    @GetMapping("/api/gaiapeeps")
    List<PeepsEntity> getPeepsItems() {
        log.info("Incoming API request: gaiapeeps");
        grafanaService.registerRequest("gaiapeeps", "all");
        List<PeepsEntity> peepsList = new ArrayList<>();
        peepsRepository.findAll().forEach(peepsList::add);
        return peepsList;
    }
}