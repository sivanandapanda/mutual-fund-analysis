package api.service;

import com.example.common.model.*;
import com.example.mutualfund.grpc.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MutualFundGrpcService extends MutinyMutualFundServiceGrpc.MutualFundServiceImplBase {

    @Inject
    Logger log;

    @Inject
    ReactiveRedisClient redisClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MutualFundService mutualFundService;

    @Inject
    StatisticsCalculator statisticsCalculator;

    private static final String MF_LIST_CACHE_KEY = "MF_LIST_CACHE_KEY";
    private static final String MF_BY_CODE_CACHE_KEY = "MF_BY_CODE_CACHE_KEY";

    @Override
    public Uni<MutualFundGrpc> getMutualFund(SchemeCodeRequest request) {
        return redisClient.hget(MF_BY_CODE_CACHE_KEY, String.valueOf(request.getSchemeCode()))
        .onItem().transform(response -> {
            try {
                var mutualFund = objectMapper.readValue(response.toString(), MutualFund.class);

                var mutualFundMetaGrpc = MutualFundMetaGrpc.newBuilder()
                        .setFundHouse(mutualFund.getMeta().getFundHouse())
                        .setSchemeCategory(mutualFund.getMeta().getSchemeCategory())
                        .setSchemeType(mutualFund.getMeta().getSchemeType())
                        .setSchemeName(mutualFund.getMeta().getSchemeName())
                        .setSchemeCode(request.getSchemeCode())
                        .build();

                var navPerDates = mutualFund.getNavPerDates().stream().map(n -> NavPerDateGrpc.newBuilder()
                        .setDate(n.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                        .setSchemeCode(request.getSchemeCode())
                        .setNav(n.getNav().doubleValue())
                        .build()).collect(Collectors.toList());

                return MutualFundGrpc.newBuilder()
                        .setMeta(mutualFundMetaGrpc)
                        .addAllNavPerDates(navPerDates)
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
                //return MutualFund.newBuilder().build();
                throw new RuntimeException();
            }
        }).ifNoItem().after(Duration.ofMillis(1)).recoverWithUni(mutualFundService.downloadMutualFundNavAndTransform(request.getSchemeCode()));
    }

    @Override
    public Uni<MutualFundStatisticsGrpc> getMutualFundStatistics(SchemeCodeRequest request) {
        log.info("getStatistics " + request.getSchemeCode());
        return mutualFundService.retrieveMutualFundNav(request.getSchemeCode())
                .onItem().transform(mutualFund -> {
                    var mutualFundStatistics = statisticsCalculator.getStatistics(mutualFund);

                    var mutualFundMeta = MutualFundMetaGrpc.newBuilder()
                            .setFundHouse(mutualFundStatistics.getMutualFundMeta().getFundHouse())
                            .setSchemeCategory(mutualFundStatistics.getMutualFundMeta().getSchemeCategory())
                            .setSchemeType(mutualFundStatistics.getMutualFundMeta().getSchemeType())
                            .setSchemeName(mutualFundStatistics.getMutualFundMeta().getSchemeName())
                            .setSchemeCode(request.getSchemeCode())
                            .build();

                    var navStatistics = mutualFundStatistics.getStatistics().getStatisticsList().stream()
                            .map(s -> NavStatisticsGrpc.newBuilder()
                                    .setDate(s.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                                    .setDays(s.getDays())
                                    .setNav(s.getNav().doubleValue())
                                    .setTenorValue(s.getTenor().getTenorValue())
                                    .setMoveValue(s.getMove().getMoveValue())
                                    .build())
                            .collect(Collectors.toList());

                    return MutualFundStatisticsGrpc.newBuilder()
                            .setMeta(mutualFundMeta)
                            .addAllStatistics(navStatistics)
                            .setPercentageIncrease(mutualFundStatistics.getPercentageIncrease())
                            .build();
                });
    }

    @Override
    public Multi<MutualFundMetaGrpc> getAllMutualFunds(Empty request) {
        return redisClient.get(MF_LIST_CACHE_KEY)
                .map(response -> {
                    try {
                        List<MutualFundMeta> readValue = objectMapper.readValue(response.toString(), new TypeReference<>() {});

                        return readValue.stream()
                                .map(r -> MutualFundMetaGrpc.newBuilder()
                                        .setSchemeCode(r.getSchemeCode())
                                        .setFundHouse(r.getFundHouse())
                                        .setSchemeType(r.getSchemeType())
                                        .setSchemeCategory(r.getSchemeCategory())
                                        .setSchemeName(r.getSchemeName())
                                        .build())
                                .collect(Collectors.toList());

                    } catch (JsonProcessingException e) {
                        log.error("Exception occurred while parsing from json", e);
                        return new ArrayList<MutualFundMetaGrpc>();
                    }
                })
                .onItem().transformToMulti(items -> Multi.createFrom().iterable(items));
    }
}
