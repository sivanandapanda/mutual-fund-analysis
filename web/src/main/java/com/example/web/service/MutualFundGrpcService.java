package com.example.web.service;

import com.example.common.model.MutualFund;
import com.example.common.model.MutualFundMeta;
import com.example.mutualfund.grpc.MutinyMutualFundSearchServiceGrpc;
import com.example.mutualfund.grpc.MutinyMutualFundServiceGrpc;
import com.example.mutualfund.grpc.SchemeCodeRequest;
import com.example.web.model.Dashboard;
import io.quarkus.grpc.runtime.annotations.GrpcService;
import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MutualFundGrpcService {

    @Inject
    @GrpcService("mf-service")
    MutinyMutualFundServiceGrpc.MutinyMutualFundServiceStub mutualFundService;

    @Inject
    @GrpcService("mf-search-service")
    MutinyMutualFundSearchServiceGrpc.MutinyMutualFundSearchServiceStub mutualFundSearchService;

    public Multi<Dashboard> getDashBoardFrom(List<Long> schemeCodes) {
        return schemeCodes.stream()
                .map(schemeCode -> {
                    return mutualFundService.getMutualFund(SchemeCodeRequest.newBuilder().setSchemeCode(schemeCode).build())
                    .onItem().transform(m -> {
                                var mutualFund = new MutualFund();
                                var mutualFundMeta = new MutualFundMeta();
                                return mutualFund;
                    });
                })
                .map(Dashboard::new)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getMutualFundMeta().getSchemeCode().compareTo(d1.getMutualFundStatistics().getMutualFundMeta().getSchemeCode()))
                .collect(Collectors.toList());
    }
}
