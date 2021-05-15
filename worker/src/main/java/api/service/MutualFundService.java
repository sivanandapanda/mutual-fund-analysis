package api.service;

import api.model.MfApiResponse;
import com.example.common.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static api.service.LocalSearchService.SEARCHABLE_MF_NAME_LIST_CACHE_KEY;
import static java.util.Objects.nonNull;

@ApplicationScoped
public class MutualFundService {

    @Inject
    Logger log;

    @Inject
    RedisClient redisClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject @RestClient
    MutualFundApiWebClient webClient;

    @Inject
    LocalSearchService searchService;

    @Inject
    StatisticsCalculator statisticsCalculator;

    private static final Lock LOCK = new ReentrantLock();
    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private static final String MF_LIST_CACHE_KEY = "MF_LIST_CACHE_KEY";
    private static final String MF_BY_CODE_CACHE_KEY = "MF_BY_CODE_CACHE_KEY";

    public List<MutualFundMeta> getAllMutualFunds() {
        Response response = redisClient.get(MF_LIST_CACHE_KEY);
        if(nonNull(response)) {
            try {
                return objectMapper.readValue(response.toString(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
            }
        }
        return null;
    }

    public synchronized MutualFund retrieveMutualFundNav(long schemeCode) {
        Response response = redisClient.hget(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode));
        if(nonNull(response)) {
            try {
                return objectMapper.readValue(response.toString(), MutualFund.class);
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
            }
        } else {
            return downloadMutualFundNav(schemeCode);
        }
        return null;
    }

    public MutualFundStatistics getStatistics(long schemeCode) {
        log.info("getStatistics " + schemeCode);
        return statisticsCalculator.getStatistics(retrieveMutualFundNav(schemeCode));
    }

    public long getSizeOfSearchableMutualFund() {
        return searchService.count();
    }

    public void clearCache() {
        redisClient.del(Arrays.asList(MF_LIST_CACHE_KEY, MF_BY_CODE_CACHE_KEY, SEARCHABLE_MF_NAME_LIST_CACHE_KEY));
    }

    public List<SearchableMutualFund> searchForMutualFund(String schemeName, int page, int size) {
        log.info("searchForMutualFund " + schemeName);
        try {
            LOCK.lock();
            if(searchService.indexSize() <= 0) {
                searchService.saveAll(webClient.getAllMfMetaData());
            }
        } finally {
            LOCK.unlock();
        }

        return searchService.searchByTag(schemeName, page, size);
    }

    private MutualFund downloadMutualFundNav(Long schemeCode) {
        MfApiResponse downloaded = webClient.getMfData(schemeCode);

        if(nonNull(downloaded) && nonNull(downloaded.getMfMetaData()) && nonNull(downloaded.getMfMetaData().getSchemeCode())) {
            List<NavPerDate> mutualFundNavPerDateList = downloaded.getNavDataList().stream().map(navData -> new NavPerDate(schemeCode,
                    LocalDate.parse(navData.getDate(), DateTimeFormatter.ofPattern(DATE_PATTERN)), BigDecimal.valueOf(Double.parseDouble(navData.getNav()))))
                    .collect(Collectors.toList());

            MutualFund mutualFund = new MutualFund()
                    .setMeta(new MutualFundMeta().setSchemeCode(schemeCode)
                            .setSchemeCategory(downloaded.getMfMetaData().getSchemeCategory())
                            .setSchemeType(downloaded.getMfMetaData().getSchemeType())
                            .setSchemeName(downloaded.getMfMetaData().getSchemeName())
                            .setFundHouse(downloaded.getMfMetaData().getFundHouse()))
                    .setNavPerDates(mutualFundNavPerDateList);

            if(nonNull(mutualFund)) {
                try {
                    redisClient.hset(Arrays.asList(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode), objectMapper.writeValueAsString(mutualFund)));
                } catch (JsonProcessingException e) {
                    log.error("Exception occurred while saving mutual fund after downloaded for {}", schemeCode, e);
                }
            }

            Response response = redisClient.get(MF_LIST_CACHE_KEY);

            try {
                List<MutualFundMeta> fundMetaList = new ArrayList<>();
                if (nonNull(response)) {
                    fundMetaList = objectMapper.readValue(response.toString(), new TypeReference<>() {});
                    fundMetaList.add(mutualFund.getMeta());
                } else {
                    if (mutualFund != null) {
                        fundMetaList = Collections.singletonList(mutualFund.getMeta());
                    }
                }
                redisClient.set(Arrays.asList(MF_LIST_CACHE_KEY, objectMapper.writeValueAsString(fundMetaList)));
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while saving mutual fund list after downloaded for {}", schemeCode, e);
            }

            return mutualFund;
        }

        return null;
    }
}
