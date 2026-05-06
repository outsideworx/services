package net.outsideworx.services.controllers.clients.soupart.ciafo;

import net.outsideworx.services.controllers.ModelVisitor;
import net.outsideworx.services.converters.clients.SoupConverter;
import net.outsideworx.services.models.clients.soup.SoupEntity;
import net.outsideworx.services.repositories.clients.SoupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
class SoupController implements ModelVisitor {
    private final SoupConverter soupConverter;

    private final SoupRepository soupRepository;

    @CacheEvict(value = "soupItems", allEntries = true)
    @PostMapping("/soupart")
    String submit(@RequestParam String category, @RequestParam Map<String, String> params, @RequestParam Map<String, MultipartFile> files) {
        log.info("Upload processor starts: soupart");
        List<SoupEntity> items = soupConverter.processItems(category, params, files);
        soupRepository.saveAll(soupConverter.filterItemsToInsert(items));
        soupConverter.filterItemsToUpdate(items).forEach(soupRepository::update);
        soupRepository.deleteAllById(soupConverter.filterIdsToDelete(items));
        return "redirect:/";
    }

    @Override
    public ModelAndView getModel() {
        ModelAndView model = new ModelAndView("clients/soupart");
        List<String> categories = List.of(
                "animation",
                "art",
                "design",
                "illustration");
        Map<String, List<SoupEntity>> items = categories
                .stream()
                .collect(Collectors.toMap(Function.identity(), soupRepository::get));
        model.addObject("categories", categories);
        model.addObject("items", items);
        return model;
    }
}