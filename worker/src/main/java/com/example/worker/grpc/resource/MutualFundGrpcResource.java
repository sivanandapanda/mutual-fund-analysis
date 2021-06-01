package com.example.worker.grpc.resource;

import com.example.mutualfund.grpc.*;
import com.example.worker.service.MutualFundService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MutualFundGrpcResource extends MutinyMutualFundServiceGrpc.MutualFundServiceImplBase {

    @Inject
    MutualFundService mutualFundService;

    @Override
    public Uni<MutualFundGrpc> getMutualFund(SchemeCodeGrpcRequest request) {
        return mutualFundService.getMutualFund(request.getSchemeCode());
    }

    @Override
    public Uni<MutualFundStatisticsGrpc> getMutualFundStatistics(SchemeCodeGrpcRequest request) {
        return mutualFundService.getMutualFundStatistics(request.getSchemeCode());
    }

    @Override
    public Multi<MutualFundMetaGrpc> getAllMutualFunds(Empty request) {
        return mutualFundService.getAllMutualFunds();
    }
}
