package api.service;

import api.model.MfApiResponse;
import com.example.mutualfund.grpc.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Singleton
public class MutualFundGrpcService extends MutinyMutualFundServiceGrpc.MutualFundServiceImplBase {

    @Inject
    Logger log;

    @Inject
    ReactiveRedisClient redisClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @RestClient
    MutualFundApiWebClient webClient;

    @Inject
    LocalSearchService searchService;

    @Inject
    StatisticsCalculator statisticsCalculator;

    private static final Lock LOCK = new ReentrantLock();
    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private static final String MF_LIST_CACHE_KEY = "MF_LIST_CACHE_KEY";
    private static final String MF_BY_CODE_CACHE_KEY = "MF_BY_CODE_CACHE_KEY";

    @Override
    public Uni<MutualFund> getMutualFund(SchemeCodeRequest request) {
        return redisClient.hget(MF_BY_CODE_CACHE_KEY, String.valueOf(request.getSchemeCode()))
        .onItem().transform(response -> {
            try {
                var mutualFund = objectMapper.readValue(response.toString(), com.example.common.model.MutualFund.class);

                var mutualFundMeta = MutualFundMeta.newBuilder()
                        .setFundHouse(mutualFund.getMeta().getFundHouse())
                        .setSchemeCategory(mutualFund.getMeta().getSchemeCategory())
                        .setSchemeType(mutualFund.getMeta().getSchemeType())
                        .setSchemeName(mutualFund.getMeta().getSchemeName())
                        .setSchemeCode(request.getSchemeCode())
                        .build();

                var navPerDates = mutualFund.getNavPerDates().stream().map(n -> NavPerDate.newBuilder()
                        .setDate(n.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                        .setSchemeCode(request.getSchemeCode())
                        .setNav(n.getNav().doubleValue())
                        .build()).collect(Collectors.toList());

                return MutualFund.newBuilder()
                        .setMeta(mutualFundMeta)
                        .addAllNavPerDates(navPerDates)
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
                //return MutualFund.newBuilder().build();
                throw new RuntimeException();
            }
        }).ifNoItem().after(Duration.ofMillis(1)).recoverWithUni(downloadMutualFundNavAndTransform(request.getSchemeCode()));
    }

    @Override
    public Uni<MutualFundStatistics> getMutualFundStatistics(SchemeCodeRequest request) {
        log.info("getStatistics " + request.getSchemeCode());
        return retrieveMutualFundNav(request.getSchemeCode())
                .onItem().transform(mutualFund -> {
                    var mutualFundStatistics = statisticsCalculator.getStatistics(mutualFund);

                    var mutualFundMeta = MutualFundMeta.newBuilder()
                            .setFundHouse(mutualFundStatistics.getMutualFundMeta().getFundHouse())
                            .setSchemeCategory(mutualFundStatistics.getMutualFundMeta().getSchemeCategory())
                            .setSchemeType(mutualFundStatistics.getMutualFundMeta().getSchemeType())
                            .setSchemeName(mutualFundStatistics.getMutualFundMeta().getSchemeName())
                            .setSchemeCode(request.getSchemeCode())
                            .build();

                    var navStatistics = mutualFundStatistics.getStatistics().getStatisticsList().stream()
                            .map(s -> NavStatistics.newBuilder()
                                    .setDate(s.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                                    .setDays(s.getDays())
                                    .setNav(s.getNav().doubleValue())
                                    .setTenorValue(s.getTenor().getTenorValue())
                                    .setMoveValue(s.getMove().getMoveValue())
                                    .build())
                            .collect(Collectors.toList());

                    return MutualFundStatistics.newBuilder()
                            .setMeta(mutualFundMeta)
                            .addAllStatistics(navStatistics)
                            .setPercentageIncrease(mutualFundStatistics.getPercentageIncrease())
                            .build();
                });
    }

    @Override
    public Multi<MutualFundMeta> getAllMutualFunds(Empty request) {
        return redisClient.get(MF_LIST_CACHE_KEY)
                .map(response -> {
                    try {
                        List<com.example.common.model.MutualFundMeta> readValue = objectMapper.readValue(response.toString(), new TypeReference<>() {});

                        return readValue.stream()
                                .map(r -> MutualFundMeta.newBuilder()
                                        .setSchemeCode(r.getSchemeCode())
                                        .setFundHouse(r.getFundHouse())
                                        .setSchemeType(r.getSchemeType())
                                        .setSchemeCategory(r.getSchemeCategory())
                                        .setSchemeName(r.getSchemeName())
                                        .build())
                                .collect(Collectors.toList());

                    } catch (JsonProcessingException e) {
                        log.error("Exception occurred while parsing from json", e);
                        return new ArrayList<MutualFundMeta>();
                    }
                })
                .onItem().transformToMulti(items -> Multi.createFrom().iterable(items));
    }

    public synchronized Uni<com.example.common.model.MutualFund> retrieveMutualFundNav(long schemeCode) {
        return redisClient.hget(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode))
                .onItem().transform(response -> {
                    try {
                        return objectMapper.readValue(response.toString(), com.example.common.model.MutualFund.class);
                    } catch (JsonProcessingException e) {
                        log.error("Exception occurred while parsing from json", e);
                        throw new RuntimeException();
                    }
                }).ifNoItem().after(Duration.ofMillis(1)).recoverWithUni(downloadMutualFundNav(schemeCode));
    }

    private Uni<MutualFund> downloadMutualFundNavAndTransform(Long schemeCode) {
        return downloadMutualFundNav(schemeCode)
                .onItem().transform(mutualFund -> {
            var mutualFundMeta = MutualFundMeta.newBuilder()
                    .setFundHouse(mutualFund.getMeta().getFundHouse())
                    .setSchemeCategory(mutualFund.getMeta().getSchemeCategory())
                    .setSchemeType(mutualFund.getMeta().getSchemeType())
                    .setSchemeName(mutualFund.getMeta().getSchemeName())
                    .setSchemeCode(schemeCode)
                    .build();

            var navPerDates = mutualFund.getNavPerDates().stream().map(n -> NavPerDate.newBuilder()
                    .setDate(n.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                    .setSchemeCode(schemeCode)
                    .setNav(n.getNav().doubleValue())
                    .build()).collect(Collectors.toList());

            return MutualFund.newBuilder()
                    .setMeta(mutualFundMeta)
                    .addAllNavPerDates(navPerDates)
                    .build();
        });
    }

    private Uni<com.example.common.model.MutualFund> downloadMutualFundNav(Long schemeCode) {
        MfApiResponse downloaded = webClient.getMfData(schemeCode);

        if(nonNull(downloaded) && nonNull(downloaded.getMfMetaData()) && nonNull(downloaded.getMfMetaData().getSchemeCode())) {
            List<com.example.common.model.NavPerDate> mutualFundNavPerDateList = downloaded.getNavDataList().stream()
                    .map(navData -> new com.example.common.model.NavPerDate(schemeCode, LocalDate.parse(navData.getDate(),
                            DateTimeFormatter.ofPattern(DATE_PATTERN)), BigDecimal.valueOf(Double.parseDouble(navData.getNav()))))
                    .collect(Collectors.toList());

            com.example.common.model.MutualFund mutualFund = new com.example.common.model.MutualFund()
                    .setMeta(new com.example.common.model.MutualFundMeta().setSchemeCode(schemeCode)
                            .setSchemeCategory(downloaded.getMfMetaData().getSchemeCategory())
                            .setSchemeType(downloaded.getMfMetaData().getSchemeType())
                            .setSchemeName(downloaded.getMfMetaData().getSchemeName())
                            .setFundHouse(downloaded.getMfMetaData().getFundHouse()))
                    .setNavPerDates(mutualFundNavPerDateList);

            if(nonNull(mutualFund)) {
                try {
                    redisClient.hset(Arrays.asList(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode), objectMapper.writeValueAsString(mutualFund))).subscribe();
                } catch (JsonProcessingException e) {
                    log.error("Exception occurred while saving mutual fund after downloaded for {}", schemeCode, e);
                }
            }

            redisClient.get(MF_LIST_CACHE_KEY).map(response -> {
                try {
                    List<com.example.common.model.MutualFundMeta> fundMetaList = new ArrayList<>();
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

                return null;
            });

            return Uni.createFrom().item(mutualFund);
        }

        return Uni.createFrom().nullItem();
    }
}
