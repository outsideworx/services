package net.outsideworx.services.controller.clients.peeps;

import net.outsideworx.services.controller.ModelVisitor;
import net.outsideworx.services.converter.clients.PeepsConverter;
import net.outsideworx.services.model.clients.peeps.PeepsEntity;
import net.outsideworx.services.repository.clients.PeepsRepository;
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
class PeepsController implements ModelVisitor {
    private final PeepsConverter peepsConverter;

    private final PeepsRepository peepsRepository;

    @PostMapping("/gaiapeeps")
    public String submit(@RequestParam Map<String, String> params) {
        log.info("Upload processor starts: gaiapeeps");
        List<PeepsEntity> items = peepsConverter.processItems(params);
        peepsRepository.saveAll(items);
        peepsRepository.deleteAllById(peepsConverter.filterIdsToDelete(items));
        return "redirect:/";
    }

    @Override
    public ModelAndView getModel() {
        ModelAndView model = new ModelAndView("clients/gaiapeeps");
        model.addObject("items", peepsRepository.findAll());
        return model;
    }
}