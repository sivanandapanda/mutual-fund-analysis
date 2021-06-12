package com.example.worker.service;

import com.example.common.model.SearchableMutualFund;
import com.example.mutualfund.grpc.MutualFundSearchResultGrpc;
import com.example.worker.web.client.MutualFundApiWebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static com.example.common.util.AppUtils.getAllTags;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class LocalSearchService {

    @Inject
    Logger log;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ReactiveRedisClient redisClient;

    @Inject
    @RestClient
    MutualFundApiWebClient webClient;

    private static final Lock LOCK = new ReentrantLock();
    private static final AtomicLong counter = new AtomicLong(0);
    public static final String SEARCHABLE_MF_NAME_LIST_CACHE_KEY = "SEARCHABLE_MF";

    private void saveAll(List<SearchableMutualFund> mutualFundList) {
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

        redisClient.hset(hashToSave).subscribe().with(item -> log.info("Saved successfully in redis"));
    }

    public Multi<MutualFundSearchResultGrpc> searchByTag(String tag) {
        log.info("searchForMutualFund " + tag);

        try {
            LOCK.lock();
            indexSize().subscribe().with(item -> {
                if (item <= 0) {
                    saveAll(webClient.getAllMfMetaData());
                }
            }, Throwable::printStackTrace);
        } finally {
            LOCK.unlock();
        }

        return redisClient.hget(SEARCHABLE_MF_NAME_LIST_CACHE_KEY, tag.trim().toLowerCase())
                .onItem()
                .transformToMulti(response -> {
                    List<MutualFundSearchResultGrpc> result = new ArrayList<>();
                    try {
                        List<SearchableMutualFund> searchableMutualFunds = objectMapper.readValue(response.toString(), new TypeReference<>() {
                        });
                        result = searchableMutualFunds.stream().map(SearchableMutualFund::convertToGrpcModel).collect(toList());
                    } catch (JsonProcessingException e) {
                        log.error("Exception occurred while parsing from json", e);
                    }
                    return Multi.createFrom().iterable(result);
                });
    }

    public long count() {
        return counter.get();
    }

    public Uni<Integer> indexSize() {
        return redisClient.hgetall(SEARCHABLE_MF_NAME_LIST_CACHE_KEY).onItem().transform(response -> response.getKeys().size());
    }
}
