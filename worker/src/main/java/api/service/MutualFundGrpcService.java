package api.service;

import com.example.common.model.MutualFundMeta;
import com.example.mutualfund.grpc.Models;
import com.example.mutualfund.grpc.MutualFundServiceGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import io.quarkus.redis.client.RedisClient;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.vertx.redis.client.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Singleton
public class MutualFundGrpcService extends MutualFundServiceGrpc.MutualFundServiceImplBase {

    @Inject
    Logger log;

    @Inject
    RedisClient redisClient;

    @Inject
    ReactiveRedisClient reactiveRedisClient;

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
    public void getAllMutualFunds(Models.Empty request, StreamObserver<Models.MutualFundMetaList> responseObserver) {
        Response response = redisClient.get(MF_LIST_CACHE_KEY);
        if (Objects.nonNull(response)) {
            try {
                List<MutualFundMeta> readValue = objectMapper.readValue(response.toString(), new TypeReference<>() {});

                var collect = readValue.stream()
                        .map(r -> Models.MutualFundMeta.newBuilder()
                                .setSchemeCode(r.getSchemeCode())
                                .setFundHouse(r.getFundHouse())
                                .setSchemeType(r.getSchemeType())
                                .setSchemeCategory(r.getSchemeCategory())
                                .setSchemeName(r.getSchemeName())
                                .build())
                        .collect(Collectors.toList());


                var build = Models.MutualFundMetaList.newBuilder()
                        .setMetaList(0, collect.get(0))
                        .build();
                responseObserver.onNext(build);
                responseObserver.onCompleted();

            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
                responseObserver.onError(e);
            }
        }
        //return Uni.createFrom().nullItem();
    }

    @Override
    public void getMutualFund(Models.SchemeCodeRequest request, StreamObserver<Models.MutualFund> responseObserver) {
        super.getMutualFund(request, responseObserver);
    }

    @Override
    public void getMutualFundStatistics(Models.SchemeCodeRequest request, StreamObserver<Models.MutualFundStatistics> responseObserver) {
        super.getMutualFundStatistics(request, responseObserver);
    }

    /*@Override
    public Uni<Models.MutualFundMetaList> getAllMutualFunds(Models.Empty request) {
        Response response = redisClient.get(MF_LIST_CACHE_KEY);
        if (nonNull(response)) {
            try {
                List<MutualFundMeta> readValue = objectMapper.readValue(response.toString(), new TypeReference<>() {
                });

                var collect = readValue.stream()
                        .map(r -> Models.MutualFundMeta.newBuilder()
                                .setSchemeCode(r.getSchemeCode())
                                .setFundHouse(r.getFundHouse())
                                .setSchemeType(r.getSchemeType())
                                .setSchemeCategory(r.getSchemeCategory())
                                .setSchemeName(r.getSchemeName())
                                .build())
                        .collect(Collectors.toList());

                var build = Models.MutualFundMetaList.newBuilder()
                        .setMetaList(0, collect.get(0))
                        .build();

            } catch (JsonProcessingException e) {
                log.error("Exception occurred while parsing from json", e);
            }
        }
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Models.MutualFund> getMutualFund(Models.SchemeCodeRequest request) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Models.MutualFundStatistics> getMutualFundStatistics(Models.SchemeCodeRequest request) {
        return Uni.createFrom().nullItem();
    }*/
}
