package application.converter.clients;

import application.model.clients.ciafo.CiafoEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

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
                .allMatch(i -> i.getId() == null && !i.getDelete());
    }

    @Test
    void filterItemsToUpdate_returnsExistingNonDeletedItems() {
        List<CiafoEntity> items = List.of(
                entity(1L, false),
                entity(2L, true),
                entity(null, false)
        );
        assertThat(converter.filterItemsToUpdate(items)).hasSize(1)
                .allMatch(i -> i.getId() != null && !i.getDelete());
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
}
