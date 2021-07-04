package com.example.web.service;

import com.example.common.model.*;
import com.example.mutualfund.grpc.MutualFundSearchServiceGrpc;
import com.example.mutualfund.grpc.MutualFundServiceGrpc;
import com.example.mutualfund.grpc.SchemeCodeGrpcRequest;
import com.example.mutualfund.grpc.SearchGrpcRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.example.common.util.AppUtils.getAllTags;
import static com.example.web.service.MutualFundMutinyGrpcService.convertFromGrpcModel;

@ApplicationScoped
public class MutualFundGrpcService {

    //todo add limit in number of requests

    @Inject
    @GrpcClient("mf-service")
    MutualFundServiceGrpc.MutualFundServiceFutureStub mutualFundService;

    @Inject
    @GrpcClient("mf-search-service")
    MutualFundSearchServiceGrpc.MutualFundSearchServiceBlockingStub searchService;


    public Uni<List<Dashboard>> getDashBoardFrom(List<Long> schemeCodes) {
        var allDashboards = schemeCodes.stream()
                .map(schemeCode -> mutualFundService.getMutualFundStatistics(SchemeCodeGrpcRequest.newBuilder().setSchemeCode(schemeCode).build()))
                .collect(Collectors.toList());

        var dashboards = allDashboards.stream().map(future -> {
            try {
                var m = future.get();
                var statistics = m.getStatisticsList().stream()
                        .map(s -> new NavStatistics(s.getDays(),
                                LocalDate.parse(s.getDate(), DateTimeFormatter.BASIC_ISO_DATE),
                                BigDecimal.valueOf(s.getNav()),
                                TenorEnum.fromValue(s.getTenorValue()),
                                Move.fromValue(s.getMoveValue())))
                        .collect(Collectors.toList());

                return new Dashboard(new MutualFundStatistics(convertFromGrpcModel(m.getMeta()), statistics, m.getPercentageIncrease()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Uni.createFrom().item(dashboards);
    }

    public Uni<List<SearchableMutualFund>> searchMutualFunds(String searchString) {
        var allSearchResults = getAllTags(searchString).stream()
                .map(s -> searchService.searchForMutualFund(SearchGrpcRequest.newBuilder().setSearchString(s).build()))
                .map(result -> {

                    List<SearchableMutualFund> searchResults = new ArrayList<>();

                    while (result.hasNext()) {
                        var m = result.next();
                        searchResults.add(new SearchableMutualFund(m.getSchemeCode(), m.getSchemeName(), m.getSearchScore()));
                    }

                    return searchResults;
                }).flatMap(Collection::stream)
                .collect(Collectors.toList());

        return Uni.createFrom().item(allSearchResults);
    }

    public Uni<List<Dashboard>> exploreMutualFunds(String searchString, int sampleSize) {
        var allSearchResults = getAllTags(searchString).stream()
                .map(s -> searchService.searchForMutualFund(SearchGrpcRequest.newBuilder().setSearchString(s).build()))
                .map(result -> {

                    List<SearchableMutualFund> searchResults = new ArrayList<>();

                    while (result.hasNext()) {
                        var m = result.next();
                        searchResults.add(new SearchableMutualFund(m.getSchemeCode(), m.getSchemeName(), m.getSearchScore()));
                    }

                    return searchResults;
                }).flatMap(Collection::stream)
                .sorted((d1, d2) -> d2.getSearchScore().compareTo(d1.getSearchScore()))
                .limit(sampleSize)
                .collect(Collectors.toList());

        var exploreResults = allSearchResults.stream()
                .map(s -> mutualFundService.getMutualFundStatistics(SchemeCodeGrpcRequest.newBuilder().setSchemeCode(s.getSchemeCode()).build()))
                .map(future -> {
                    try {
                        var m = future.get();
                        var statistics = m.getStatisticsList().stream()
                                .map(s -> new NavStatistics(s.getDays(),
                                        LocalDate.parse(s.getDate(), DateTimeFormatter.BASIC_ISO_DATE),
                                        BigDecimal.valueOf(s.getNav()),
                                        TenorEnum.fromValue(s.getTenorValue()),
                                        Move.fromValue(s.getMoveValue())))
                                .collect(Collectors.toList());

                        return new Dashboard(new MutualFundStatistics(convertFromGrpcModel(m.getMeta()), statistics, m.getPercentageIncrease()));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getPercentageIncrease().compareTo(d1.getMutualFundStatistics().getPercentageIncrease()))
                .collect(Collectors.toList());

        return Uni.createFrom().item(exploreResults);
    }
}
