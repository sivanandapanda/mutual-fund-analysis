package com.example.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AppUtils {

    private static final String SEARCH_TAG_PATTERN = "[a-zA-Z]*";

    public static Double score(String schemeName, List<String> tagList) {
        Map<String, Long> scoreMap = new ConcurrentHashMap<>();

        tagList.parallelStream()
                .map(AppUtils::getAllTags)
                .flatMap(List::parallelStream)
                .forEach(t -> scoreMap.put(t, (long) t.length()));

        for (int i = 0; i < tagList.size(); i++) {
            scoreMap.put(tagList.get(i), 10000L + (tagList.size() - i) * 100L);
        }

        return scoreMap.entrySet().stream().parallel()
                .filter(e -> schemeName.toLowerCase().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(0L, Long::sum)
                .doubleValue();
    }

    public static List<String> getAllTags(String str) {
        return Stream.of(str.split(" "))
                .parallel()
                .map(Collections::singletonList)
                .flatMap(List::stream)
                .filter(tag -> tag.length() > 3)
                .filter(tag -> tag.matches(SEARCH_TAG_PATTERN))
                .map(String::trim).map(String::toLowerCase)
                .collect(toList());
    }
}
