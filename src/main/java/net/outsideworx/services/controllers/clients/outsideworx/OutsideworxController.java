package net.outsideworx.services.controllers.clients.outsideworx;

import net.outsideworx.services.configuration.utils.Properties;
import net.outsideworx.services.controllers.ModelVisitor;
import net.outsideworx.services.converters.clients.TunnelConverter;
import net.outsideworx.services.models.clients.worx.TunnelEntity;
import net.outsideworx.services.repositories.clients.TunnelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
class OutsideworxController implements ModelVisitor {
    private final Properties properties;

    private final TunnelConverter tunnelConverter;

    private final TunnelRepository tunnelRepository;

    @PostMapping("/outsideworx")
    String submit(@RequestParam Map<String, String> params) {
        log.info("Upload processor starts: outsideworx");
        List<TunnelEntity> items = tunnelConverter.processItems(params);
        tunnelRepository.saveAll(items);
        tunnelRepository.deleteAllById(tunnelConverter.filterIdsToDelete(items));
        return "redirect:/";
    }

    @Override
    public ModelAndView getModel() {
        ModelAndView model = new ModelAndView("clients/outsideworx");
        model.addObject("items", tunnelRepository.findAll());
        model.addObject("knockUrl", properties.getServices().get("operator").getUrl());
        return model;
    }
}
