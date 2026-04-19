package application.converter.clients;

import application.model.clients.ciafo.CiafoEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CiafoConverterTest {
    private final CiafoConverter converter = new CiafoConverter();

    private CiafoEntity entity(Long id, boolean delete) {
        CiafoEntity e = new CiafoEntity();
        e.setId(id);
        e.setDelete(delete);
        return e;
    }

    @Test
    void filterItemsToInsert_returnsNewNonDeletedItems() {
        List<CiafoEntity> items = List.of(
                entity(null, false),
                entity(null, true),
                entity(1L, false)
        );
        assertThat(converter.filterItemsToInsert(items)).hasSize(1)
                .allMatch(i -> Objects.isNull(i.getId()) && !i.getDelete());
    }

    @Test
    void filterItemsToUpdate_returnsExistingNonDeletedItems() {
        List<CiafoEntity> items = List.of(
                entity(1L, false),
                entity(2L, true),
                entity(null, false)
        );
        assertThat(converter.filterItemsToUpdate(items)).hasSize(1)
                .allMatch(i -> Objects.nonNull(i.getId()) && !i.getDelete());
    }

    @Test
    void filterIdsToDelete_returnsIdsOfExistingDeletedItems() {
        List<CiafoEntity> items = List.of(
                entity(1L, true),
                entity(2L, false),
                entity(null, true)
        );
        assertThat(converter.filterIdsToDelete(items)).containsExactly(1L);
    }

    @Test
    void processItems_whenEmptyParams_returnsEmptyList() {
        assertThat(converter.processItems("Furniture", Map.of(), Map.of())).isEmpty();
    }

    @Test
    void processItems_mapsFieldsCorrectly() {
        Map<String, String> params = Map.of(
                "items[0].id", "10",
                "items[0].description", "A chair",
                "items[0].delete", ""
        );

        List<CiafoEntity> result = converter.processItems("Furniture", params, Map.of());

        assertThat(result).hasSize(1);
        CiafoEntity item = result.getFirst();
        assertThat(item.getId()).isEqualTo(10L);
        assertThat(item.getCategory()).isEqualTo("Furniture");
        assertThat(item.getDescription()).isEqualTo("A chair");
        assertThat(item.getDelete()).isFalse();
    }

    @Test
    void processItems_whenIdIsBlank_setsIdToNull() {
        Map<String, String> params = Map.of("items[0].description", "desc");

        CiafoEntity item = converter.processItems("Furniture", params, Map.of()).getFirst();

        assertThat(item.getId()).isNull();
    }

    @Test
    void processItems_whenDeleteIsOn_setsDeleteTrue() {
        Map<String, String> params = Map.of(
                "items[0].id", "5",
                "items[0].delete", "on"
        );

        CiafoEntity item = converter.processItems("Furniture", params, Map.of()).getFirst();

        assertThat(item.getDelete()).isTrue();
    }

    @Test
    void processItems_whenDescriptionIsBlank_setsDescriptionToNull() {
        Map<String, String> params = Map.of(
                "items[0].id", "1",
                "items[0].description", "   "
        );

        CiafoEntity item = converter.processItems("Furniture", params, Map.of()).getFirst();

        assertThat(item.getDescription()).isNull();
    }

    @Test
    void processItems_withMultipleIterators_returnsCorrectItemCount() {
        Map<String, String> params = Map.of(
                "items[0].description", "first",
                "items[1].description", "second"
        );

        assertThat(converter.processItems("Furniture", params, Map.of())).hasSize(2);
    }
}