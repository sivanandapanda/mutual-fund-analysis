package com.example.web.service;

import com.example.common.model.*;
import com.example.mutualfund.grpc.*;
import com.example.common.model.Dashboard;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.common.util.AppUtils.getAllTags;
import static com.example.common.util.AppUtils.score;

//@ApplicationScoped
public class MutualFundMutinyGrpcService {

    @Inject
    @GrpcClient("mf-service")
    MutinyMutualFundServiceGrpc.MutinyMutualFundServiceStub mutualFundService;

    @Inject
    @GrpcClient("mf-search-service")
    MutinyMutualFundSearchServiceGrpc.MutinyMutualFundSearchServiceStub mutualFundSearchService;

    public Uni<List<Dashboard>> getDashBoardFrom(List<Long> schemeCodes) {
        return Multi.createFrom().iterable(schemeCodes)
                .onItem().transformToUni(schemeCode -> mutualFundService.getMutualFundStatistics(SchemeCodeGrpcRequest.newBuilder().setSchemeCode(schemeCode).build())
                        .onItem().transform(m -> {
                            var statistics = m.getStatisticsList().stream()
                                    .map(s -> new NavStatistics(s.getDays(),
                                            LocalDate.parse(s.getDate(), DateTimeFormatter.BASIC_ISO_DATE),
                                            BigDecimal.valueOf(s.getNav()),
                                            TenorEnum.fromValue(s.getTenorValue()),
                                            Move.fromValue(s.getMoveValue())))
                                    .collect(Collectors.toList());

                            return new Dashboard(new MutualFundStatistics(convertFromGrpcModel(m.getMeta()), statistics, m.getPercentageIncrease()));
                        }))
                .merge()
                .collect()
                .asList()
                .map(list -> list.stream()
                        .sorted((d1, d2) -> d2.getMutualFundStatistics().getMutualFundMeta().getSchemeCode().compareTo(d1.getMutualFundStatistics().getMutualFundMeta().getSchemeCode()))
                        .collect(Collectors.toList()));
    }

    public Uni<List<SearchableMutualFund>> searchMutualFunds(String searchString) {
        return Multi.createFrom().iterable(getAllTags(searchString))
                .onItem()
                .transformToMulti(tag ->
                        mutualFundSearchService.searchForMutualFund(SearchGrpcRequest.newBuilder().setSearchString(tag).build())
                                .onItem().transform(m -> new SearchableMutualFund(m.getSchemeCode(), m.getSchemeName(), m.getSearchScore())))
                .merge()
                .collect()
                .asList()
                .map(list -> list.stream().sorted((s1, s2) -> s2.getSearchScore().compareTo(s1.getSearchScore())).collect(Collectors.toList()));
    }

    public Uni<List<Dashboard>> exploreMutualFunds(String searchString, int sampleSize) {
        var allTags = getAllTags(searchString);
        return Multi.createFrom().iterable(allTags)
                .onItem()
                .transformToMulti(tag ->
                        mutualFundSearchService.searchForMutualFund(SearchGrpcRequest.newBuilder().setSearchString(searchString).build())
                                .onItem().transform(m -> new SearchableMutualFund(m.getSchemeCode(), m.getSchemeName(), score(m.getSchemeName(), allTags))))
                .merge()
                .collect()
                .asList()
                .map(list -> list.stream()
                        .sorted((d1, d2) -> d2.getSearchScore().compareTo(d1.getSearchScore()))
                        .limit(sampleSize)
                        .collect(Collectors.toList()))
                .onItem().transformToUni(searchResults ->
                        Multi.createFrom().iterable(searchResults).onItem().transformToUni(searchableMutualFund ->
                                mutualFundService.getMutualFundStatistics(SchemeCodeGrpcRequest.newBuilder().setSchemeCode(searchableMutualFund.getSchemeCode()).build())
                                        .onItem()
                                        .transform(m -> {
                                            var statistics = m.getStatisticsList().stream()
                                                    .map(ss -> new NavStatistics(ss.getDays(),
                                                            LocalDate.parse(ss.getDate(), DateTimeFormatter.BASIC_ISO_DATE),
                                                            BigDecimal.valueOf(ss.getNav()),
                                                            TenorEnum.fromValue(ss.getTenorValue()),
                                                            Move.fromValue(ss.getMoveValue())))
                                                    .collect(Collectors.toList());

                                            return new Dashboard(new MutualFundStatistics(convertFromGrpcModel(m.getMeta()), statistics, m.getPercentageIncrease()));
                                        }))
                                .merge()
                                .collect()
                                .asList()
                                .map(list -> list.stream()
                                        .sorted((d1, d2) -> d2.getMutualFundStatistics().getPercentageIncrease().compareTo(d1.getMutualFundStatistics().getPercentageIncrease()))
                                        .collect(Collectors.toList()))
                );
    }

    static MutualFundMeta convertFromGrpcModel(MutualFundMetaGrpc m) {
        var mutualFundMeta = new MutualFundMeta();
        mutualFundMeta.setFundHouse(m.getFundHouse());
        mutualFundMeta.setSchemeCategory(m.getSchemeCategory());
        mutualFundMeta.setSchemeCode(m.getSchemeCode());
        mutualFundMeta.setSchemeName(m.getSchemeName());
        mutualFundMeta.setSchemeType(m.getSchemeType());
        return mutualFundMeta;
    }
}
