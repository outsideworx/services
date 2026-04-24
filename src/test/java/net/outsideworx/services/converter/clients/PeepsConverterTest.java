package net.outsideworx.services.converter.clients;

import net.outsideworx.services.model.clients.peeps.PeepsEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PeepsConverterTest {
    private final PeepsConverter converter = new PeepsConverter();

    @Test
    void processItems_mapsFieldsCorrectly() {
        Map<String, String> params = Map.of(
                "items[0].id", "42",
                "items[0].title", "My Title",
                "items[0].link", "https://example.com",
                "items[0].delete", ""
        );

        List<PeepsEntity> result = converter.processItems(params);

        assertThat(result).hasSize(1);
        PeepsEntity item = result.getFirst();
        assertThat(item.getId()).isEqualTo(42L);
        assertThat(item.getTitle()).isEqualTo("My Title");
        assertThat(item.getLink()).isEqualTo("https://example.com");
        assertThat(item.getDelete()).isFalse();
    }

    @Test
    void processItems_whenIdIsBlank_setsIdToNull() {
        Map<String, String> params = Map.of("items[0].title", "T");

        List<PeepsEntity> result = converter.processItems(params);

        assertThat(result.getFirst().getId()).isNull();
    }

    @Test
    void processItems_whenDeleteIsOn_setsDeleteTrue() {
        Map<String, String> params = Map.of(
                "items[0].id", "1",
                "items[0].delete", "on"
        );

        List<PeepsEntity> result = converter.processItems(params);

        assertThat(result.getFirst().getDelete()).isTrue();
    }

    @Test
    void filterIdsToDelete_returnsOnlyMarkedItems() {
        PeepsEntity toDelete = new PeepsEntity();
        toDelete.setId(1L);
        toDelete.setDelete(true);

        PeepsEntity toKeep = new PeepsEntity();
        toKeep.setId(2L);
        toKeep.setDelete(false);

        PeepsEntity newItem = new PeepsEntity();
        newItem.setDelete(true);

        assertThat(converter.filterIdsToDelete(List.of(toDelete, toKeep, newItem))).containsExactly(1L);
    }
}