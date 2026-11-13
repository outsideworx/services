package net.outsideworx.services.converters.clients;

import net.outsideworx.services.models.clients.worx.TunnelEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TunnelConverterTest {
    private final TunnelConverter converter = new TunnelConverter();

    @Test
    void processItems_mapsFieldsCorrectly() {
        Map<String, String> params = Map.of(
                "items[0].id", "42",
                "items[0].name", "operator-knock-token",
                "items[0].value", "secret",
                "items[0].delete", ""
        );

        List<TunnelEntity> result = converter.processItems(params);

        assertThat(result).hasSize(1);
        TunnelEntity item = result.getFirst();
        assertThat(item.getId()).isEqualTo(42L);
        assertThat(item.getName()).isEqualTo("operator-knock-token");
        assertThat(item.getValue()).isEqualTo("secret");
        assertThat(item.getDelete()).isFalse();
    }

    @Test
    void processItems_whenIdIsBlank_setsIdToNull() {
        Map<String, String> params = Map.of("items[0].name", "key");

        List<TunnelEntity> result = converter.processItems(params);

        assertThat(result.getFirst().getId()).isNull();
    }

    @Test
    void processItems_whenDeleteIsOn_setsDeleteTrue() {
        Map<String, String> params = Map.of(
                "items[0].id", "1",
                "items[0].delete", "on"
        );

        List<TunnelEntity> result = converter.processItems(params);

        assertThat(result.getFirst().getDelete()).isTrue();
    }

    @Test
    void filterIdsToDelete_returnsOnlyMarkedItems() {
        TunnelEntity toDelete = new TunnelEntity();
        toDelete.setId(1L);
        toDelete.setDelete(true);

        TunnelEntity toKeep = new TunnelEntity();
        toKeep.setId(2L);
        toKeep.setDelete(false);

        TunnelEntity newItem = new TunnelEntity();
        newItem.setDelete(true);

        assertThat(converter.filterIdsToDelete(List.of(toDelete, toKeep, newItem))).containsExactly(1L);
    }
}
