package api.service;

import api.model.MfApiResponse;
import com.example.common.model.MutualFund;
import com.example.common.model.MutualFundMeta;
import com.example.common.model.NavPerDate;
import com.example.mutualfund.grpc.MutualFundGrpc;
import com.example.mutualfund.grpc.MutualFundMetaGrpc;
import com.example.mutualfund.grpc.NavPerDateGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static api.service.LocalSearchService.SEARCHABLE_MF_NAME_LIST_CACHE_KEY;
import static java.util.Objects.nonNull;

@ApplicationScoped
public class MutualFundService {

    @Inject
    Logger log;

    @Inject
    ReactiveRedisClient redisClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject @RestClient
    MutualFundApiWebClient webClient;

    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private static final String MF_LIST_CACHE_KEY = "MF_LIST_CACHE_KEY";
    private static final String MF_BY_CODE_CACHE_KEY = "MF_BY_CODE_CACHE_KEY";

    synchronized Uni<MutualFund> retrieveMutualFundNav(long schemeCode) {
        return redisClient.hget(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode))
                .onItem().transform(response -> {
                    try {
                        return objectMapper.readValue(response.toString(), MutualFund.class);
                    } catch (JsonProcessingException e) {
                        log.error("Exception occurred while parsing from json", e);
                        throw new RuntimeException();
                    }
                }).ifNoItem().after(Duration.ofMillis(1)).recoverWithUni(downloadMutualFundNav(schemeCode));
    }

    Uni<MutualFundGrpc> downloadMutualFundNavAndTransform(Long schemeCode) {
        return downloadMutualFundNav(schemeCode)
                .onItem().transform(mutualFund -> {
                    var mutualFundMeta = MutualFundMetaGrpc.newBuilder()
                            .setFundHouse(mutualFund.getMeta().getFundHouse())
                            .setSchemeCategory(mutualFund.getMeta().getSchemeCategory())
                            .setSchemeType(mutualFund.getMeta().getSchemeType())
                            .setSchemeName(mutualFund.getMeta().getSchemeName())
                            .setSchemeCode(schemeCode)
                            .build();

                    var navPerDates = mutualFund.getNavPerDates().stream().map(n -> NavPerDateGrpc.newBuilder()
                            .setDate(n.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                            .setSchemeCode(schemeCode)
                            .setNav(n.getNav().doubleValue())
                            .build()).collect(Collectors.toList());

                    return MutualFundGrpc.newBuilder()
                            .setMeta(mutualFundMeta)
                            .addAllNavPerDates(navPerDates)
                            .build();
                });
    }

    private Uni<MutualFund> downloadMutualFundNav(Long schemeCode) {
        MfApiResponse downloaded = webClient.getMfData(schemeCode);

        if(nonNull(downloaded) && nonNull(downloaded.getMfMetaData()) && nonNull(downloaded.getMfMetaData().getSchemeCode())) {
            List<NavPerDate> mutualFundNavPerDateList = downloaded.getNavDataList().stream()
                    .map(navData -> new NavPerDate(schemeCode, LocalDate.parse(navData.getDate(),
                            DateTimeFormatter.ofPattern(DATE_PATTERN)), BigDecimal.valueOf(Double.parseDouble(navData.getNav()))))
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
                    redisClient.hset(Arrays.asList(MF_BY_CODE_CACHE_KEY, String.valueOf(schemeCode), objectMapper.writeValueAsString(mutualFund))).subscribe();
                } catch (JsonProcessingException e) {
                    log.error("Exception occurred while saving mutual fund after downloaded for {}", schemeCode, e);
                }
            }

            redisClient.get(MF_LIST_CACHE_KEY).map(response -> {
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

                return null;
            });

            return Uni.createFrom().item(mutualFund);
        }

        return Uni.createFrom().nullItem();
    }

    public void clearCache() {
        redisClient.del(Arrays.asList(MF_LIST_CACHE_KEY, MF_BY_CODE_CACHE_KEY, SEARCHABLE_MF_NAME_LIST_CACHE_KEY));
    }

}
