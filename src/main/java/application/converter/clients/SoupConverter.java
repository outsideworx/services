package application.converter.clients;

import application.converter.ImageConverter;
import application.model.clients.ciafo.CiafoEntity;
import application.model.clients.soup.SoupEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class SoupConverter extends ImageConverter {

    public List<SoupEntity> filterItemsToInsert(List<SoupEntity> items) {
        return items
                .stream()
                .filter(item -> Objects.isNull(item.getId()))
                .filter(item -> !item.getDelete())
                .toList();
    }

    public List<SoupEntity> filterItemsToUpdate(List<SoupEntity> items) {
        return items
                .stream()
                .filter(item -> Objects.nonNull(item.getId()))
                .filter(item -> !item.getDelete())
                .toList();
    }

    public List<Long> filterIdsToDelete(List<SoupEntity> items) {
        return items
                .stream()
                .filter(item -> Objects.nonNull(item.getId()))
                .filter(SoupEntity::getDelete)
                .map(SoupEntity::getId)
                .toList();
    }

    public List<SoupEntity> processItems(String category, Map<String, String> params, Map<String, MultipartFile> files) {
        return getIterators(params)
                .stream()
                .map(iterator -> {
                    SoupEntity item = new SoupEntity();
                    item.setId(getValue(params, iterator, "id")
                            .filter(id -> !StringUtils.isEmptyOrWhitespace(id))
                            .map(Long::valueOf)
                            .orElse(null));
                    item.setCategory(category);
                    item.setDelete(getValue(params, iterator, "delete")
                            .filter(delete -> !StringUtils.isEmptyOrWhitespace(delete))
                            .map("on"::equals)
                            .orElse(false));
                    item.setDescription(getValue(params, iterator, "description")
                            .filter(description -> !StringUtils.isEmptyOrWhitespace(description))
                            .orElse(null));
                    item.setImage(getImage(files, iterator, "image"));
                    item.setThumbnail(getThumbnail(files, iterator, "thumbnail"));
                    item.setLink(getValue(params, iterator, "link")
                            .filter(link -> !StringUtils.isEmptyOrWhitespace(link))
                            .orElse(null));
                    return item;
                })
                .toList();
    }
}