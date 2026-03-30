package application.converter;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemsConverterTest {
    private static class TestConverter extends ItemsConverter {
        public <T> Optional<T> testGetValue(Map<String, T> params, int iterator, String field) {
            return getValue(params, iterator, field);
        }

        public List<Integer> testGetIterators(Map<String, String> params) {
            return getIterators(params);
        }
    }

    private final TestConverter converter = new TestConverter();

    @Test
    void getValue_whenKeyExists_returnsValue() {
        Map<String, String> params = Map.of("items[0].title", "Hello");
        assertThat(converter.testGetValue(params, 0, "title")).contains("Hello");
    }

    @Test
    void getValue_whenKeyDoesNotExist_returnsEmpty() {
        Map<String, String> params = Map.of("items[0].title", "Hello");
        assertThat(converter.testGetValue(params, 1, "title")).isEmpty();
    }

    @Test
    void getIterators_returnsDistinctIterators() {
        Map<String, String> params = Map.of(
                "items[0].title", "A",
                "items[0].link", "url",
                "items[1].title", "B"
        );
        assertThat(converter.testGetIterators(params)).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    void getIterators_whenNoIteratorKeys_returnsEmptyList() {
        Map<String, String> params = Map.of("other", "value");
        assertThat(converter.testGetIterators(params)).isEmpty();
    }
}
