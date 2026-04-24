package net.outsideworx.services.converter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemsConverter {
    private final Pattern iteratorPattern = Pattern.compile("(?<=items\\[)[0-9]+(?=]\\.)");

    protected <T> Optional<T> getValue(Map<String, T> params, int iterator, String field) {
        return params
                .entrySet()
                .stream()
                .filter(entry -> String.format("items[%d].%s", iterator, field).equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .findAny();
    }

    protected List<Integer> getIterators(Map<String, String> params) {
        return params
                .keySet()
                .stream()
                .map(iteratorPattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(0))
                .map(Integer::valueOf)
                .distinct()
                .toList();
    }
}