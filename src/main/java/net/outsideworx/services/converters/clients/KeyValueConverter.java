package net.outsideworx.services.converters.clients;

import net.outsideworx.services.converters.ItemsConverter;
import net.outsideworx.services.models.clients.worx.KeyValueEntity;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public final class KeyValueConverter extends ItemsConverter {
    public List<Long> filterIdsToDelete(List<KeyValueEntity> items) {
        return items
                .stream()
                .filter(item -> Objects.nonNull(item.getId()))
                .filter(KeyValueEntity::getDelete)
                .map(KeyValueEntity::getId)
                .toList();
    }

    public List<KeyValueEntity> processItems(Map<String, String> params) {
        return getIterators(params)
                .stream()
                .map(iterator -> {
                    KeyValueEntity item = new KeyValueEntity();
                    item.setId(getValue(params, iterator, "id")
                            .filter(id -> !StringUtils.isEmptyOrWhitespace(id))
                            .map(Long::valueOf)
                            .orElse(null));
                    item.setDelete(getValue(params, iterator, "delete")
                            .filter(delete -> !StringUtils.isEmptyOrWhitespace(delete))
                            .map("on"::equals)
                            .orElse(false));
                    item.setName(getValue(params, iterator, "name")
                            .filter(name -> !StringUtils.isEmptyOrWhitespace(name))
                            .orElse(null));
                    item.setValue(getValue(params, iterator, "value")
                            .filter(value -> !StringUtils.isEmptyOrWhitespace(value))
                            .orElse(null));
                    return item;
                })
                .toList();
    }
}
