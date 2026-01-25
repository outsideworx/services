package application.controller.clients.soupart.ciafo;

import application.controller.ModelVisitor;
import application.converter.clients.SoupConverter;
import application.model.clients.soup.SoupEntity;
import application.repository.clients.SoupRepository;
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
    public String submit(@RequestParam String category, @RequestParam Map<String, String> params, @RequestParam Map<String, MultipartFile> files) {
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
                .collect(Collectors.toMap(Function.identity(), soupRepository::getThumbnailsByCategory));
        model.addObject("categories", categories);
        model.addObject("items", items);
        return model;
    }
}