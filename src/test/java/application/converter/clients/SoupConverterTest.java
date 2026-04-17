package application.converter.clients;

import application.model.clients.soup.SoupEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SoupConverterTest {
    private final SoupConverter converter = new SoupConverter();

    private SoupEntity entity(Long id, boolean delete) {
        SoupEntity e = new SoupEntity();
        e.setId(id);
        e.setDelete(delete);
        return e;
    }

    @Test
    void filterItemsToInsert_returnsNewNonDeletedItems() {
        List<SoupEntity> items = List.of(
                entity(null, false),
                entity(null, true),
                entity(1L, false)
        );
        assertThat(converter.filterItemsToInsert(items)).hasSize(1)
                .allMatch(i -> i.getId() == null && !i.getDelete());
    }

    @Test
    void filterItemsToUpdate_returnsExistingNonDeletedItems() {
        List<SoupEntity> items = List.of(
                entity(1L, false),
                entity(2L, true),
                entity(null, false)
        );
        assertThat(converter.filterItemsToUpdate(items)).hasSize(1)
                .allMatch(i -> i.getId() != null && !i.getDelete());
    }

    @Test
    void filterIdsToDelete_returnsIdsOfExistingDeletedItems() {
        List<SoupEntity> items = List.of(
                entity(1L, true),
                entity(2L, false),
                entity(null, true)
        );
        assertThat(converter.filterIdsToDelete(items)).containsExactly(1L);
    }

    @Test
    void processItems_whenEmptyParams_returnsEmptyList() {
        assertThat(converter.processItems("Art", Map.of(), Map.of())).isEmpty();
    }

    @Test
    void processItems_mapsFieldsCorrectly() {
        Map<String, String> params = Map.of(
                "items[0].id", "7",
                "items[0].description", "A painting",
                "items[0].link", "https://example.com",
                "items[0].delete", ""
        );

        List<SoupEntity> result = converter.processItems("Art", params, Map.of());

        assertThat(result).hasSize(1);
        SoupEntity item = result.getFirst();
        assertThat(item.getId()).isEqualTo(7L);
        assertThat(item.getCategory()).isEqualTo("Art");
        assertThat(item.getDescription()).isEqualTo("A painting");
        assertThat(item.getLink()).isEqualTo("https://example.com");
        assertThat(item.getDelete()).isFalse();
    }

    @Test
    void processItems_whenIdIsBlank_setsIdToNull() {
        Map<String, String> params = Map.of("items[0].description", "desc");

        SoupEntity item = converter.processItems("Art", params, Map.of()).getFirst();

        assertThat(item.getId()).isNull();
    }

    @Test
    void processItems_whenDeleteIsOn_setsDeleteTrue() {
        Map<String, String> params = Map.of(
                "items[0].id", "3",
                "items[0].delete", "on"
        );

        SoupEntity item = converter.processItems("Art", params, Map.of()).getFirst();

        assertThat(item.getDelete()).isTrue();
    }

    @Test
    void processItems_whenLinkIsBlank_setsLinkToNull() {
        Map<String, String> params = Map.of(
                "items[0].id", "1",
                "items[0].link", "   "
        );

        SoupEntity item = converter.processItems("Art", params, Map.of()).getFirst();

        assertThat(item.getLink()).isNull();
    }

    @Test
    void processItems_withMultipleIterators_returnsCorrectItemCount() {
        Map<String, String> params = Map.of(
                "items[0].description", "first",
                "items[1].description", "second"
        );

        assertThat(converter.processItems("Art", params, Map.of())).hasSize(2);
    }
}