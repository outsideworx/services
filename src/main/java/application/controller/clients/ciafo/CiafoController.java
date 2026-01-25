package application.controller.clients.ciafo;

import application.controller.ModelVisitor;
import application.converter.clients.CiafoConverter;
import application.model.clients.ciafo.CiafoEntity;
import application.model.clients.ciafo.mapping.CiafoThumbnails;
import application.repository.clients.CiafoRepository;
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
class CiafoController implements ModelVisitor {
    private final CiafoConverter ciafoConverter;

    private final CiafoRepository ciafoRepository;

    @CacheEvict(value = "ciafoItems", allEntries = true)
    @PostMapping("/come-in-and-find-out")
    public String submit(@RequestParam String category, @RequestParam Map<String, String> params, @RequestParam Map<String, MultipartFile> files) {
        log.info("Upload processor starts: come-in-and-find-out");
        List<CiafoEntity> items = ciafoConverter.processItems(category, params, files);
        ciafoRepository.saveAll(ciafoConverter.filterItemsToInsert(items));
        ciafoConverter.filterItemsToUpdate(items).forEach(ciafoRepository::update);
        ciafoRepository.deleteAllById(ciafoConverter.filterIdsToDelete(items));
        return "redirect:/";
    }

    @Override
    public ModelAndView getModel() {
        ModelAndView model = new ModelAndView("clients/come-in-and-find-out");
        List<String> categories = List.of(
                "Furniture",
                "Clothing",
                "Jewelry",
                "Accessories",
                "Art-Print",
                "Asiatica",
                "Pirate-stuff",
                "Curiosity",
                "Vehicles");
        Map<String, List<CiafoThumbnails>> items = categories
                .stream()
                .collect(Collectors.toMap(Function.identity(), ciafoRepository::getThumbnailsByCategory));
        model.addObject("categories", categories);
        model.addObject("items", items);
        return model;
    }
}