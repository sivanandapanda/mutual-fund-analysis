package api.service;

import com.example.common.model.SearchableMutualFund;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class LocalSearchService {

    @Inject
    Logger log;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    RedisClient redisClient;

    private static final String SEARCH_TAG_PATTERN = "[a-zA-Z]*";
    private static final AtomicLong counter = new AtomicLong(0);
    static final String SEARCHABLE_MF_NAME_LIST_CACHE_KEY = "SEARCHABLE_MF";

    public void saveAll(List<SearchableMutualFund> mutualFundList) {
        Map<String, List<SearchableMutualFund>> cache = new HashMap<>();
        counter.set(0);

        mutualFundList.parallelStream().forEach(mf -> {
            counter.incrementAndGet();
            getAllTags(mf.getSchemeName())
                    .forEach(tag -> {
                        List<SearchableMutualFund> list = cache.getOrDefault(tag, new ArrayList<>());
                        list.add(mf);
                        cache.put(tag, list);
                    });
        });
        List<String> hashToSave = new ArrayList<>();

        hashToSave.add(SEARCHABLE_MF_NAME_LIST_CACHE_KEY);
        cache.forEach((key, value) -> {
            hashToSave.add(key);
            try {
                hashToSave.add(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing for key {} to json", key, e);
            }
        });

        redisClient.hset(hashToSave);
    }

    public List<SearchableMutualFund> searchByTag(String tags, int page, int size) {
        try {
            List<String> tagList = Stream.of(tags.split(" ")).map(String::toLowerCase).collect(toList());

            List<SearchableMutualFund> search = tagList.parallelStream()
                    .map(t -> redisClient.hget(SEARCHABLE_MF_NAME_LIST_CACHE_KEY, t.trim().toLowerCase()))
                    .filter(Objects::nonNull)
                    .map(this::parseListOfSearchableMutualFunds)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::parallelStream)
                    .distinct()
                    .collect(toList());

            List<SearchableMutualFund> fastSearch = search.parallelStream()
                    .filter(s -> tagList.parallelStream().allMatch(t -> s.getSchemeName().toLowerCase().contains(t)))
                    .collect(toList());

            if(fastSearch.size() > page * size) {
                return fastSearch.parallelStream()
                        .sorted((b1, b2) -> toIntExact(score(b2, tagList)) - toIntExact(score(b1, tagList)))
                        .skip((long) (page - 1) * size)
                        .limit(size)
                        .collect(toList());
            } else {
                List<String> subList = tagList.subList(0, tagList.size() - 1);

                List<SearchableMutualFund> secondFastSearch = search.parallelStream()
                        .filter(s -> subList.parallelStream().allMatch(t -> s.getSchemeName().toLowerCase().contains(t)))
                        .collect(toList());

                fastSearch.addAll(secondFastSearch);

                return fastSearch.parallelStream()
                        .distinct()
                        .sorted((b1, b2) -> toIntExact(score(b2, tagList)) - toIntExact(score(b1, tagList)))
                        .skip((long) (page - 1) * size)
                        .limit(size)
                        .collect(toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<SearchableMutualFund> parseListOfSearchableMutualFunds(Response s) {
        try {
            return objectMapper.readValue(s.toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Exception occurred while parsing from json", e);
            return null;
        }
    }

    private long score(SearchableMutualFund mutualFund, List<String> tagList) {
        Map<String, Long> scoreMap = new ConcurrentHashMap<>();

        tagList.parallelStream()
                .map(this::getAllTags)
                .flatMap(List::parallelStream)
                .forEach(t -> scoreMap.put(t, (long) t.length()));

        for (int i = 0; i < tagList.size(); i++) {
            scoreMap.put(tagList.get(i), 10000L + (tagList.size() - i) * 100L);
        }

        return scoreMap.entrySet().stream().parallel()
                .filter(e -> mutualFund.getSchemeName().toLowerCase().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(0L, Long::sum);
    }

    public synchronized long count() {
        if(counter.get() == 0) {
            Response response = redisClient.hgetall(SEARCHABLE_MF_NAME_LIST_CACHE_KEY);
            long count = response.getKeys().stream()
                    .map(response::get)
                    .map(this::parseListOfSearchableMutualFunds)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .distinct()
                    .count();
            counter.compareAndSet(0, count);
        }

        return counter.get();
    }

    public long indexSize() {
        Response response = redisClient.hgetall(SEARCHABLE_MF_NAME_LIST_CACHE_KEY);
        if(Objects.nonNull(response)) {
            return response.getKeys().size();
        }

        return 0;
    }

    private List<String> getAllTags(String str) {
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
