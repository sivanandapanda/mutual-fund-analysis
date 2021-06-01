package com.example.worker.grpc.resource;

import com.example.mutualfund.grpc.MutinyMutualFundSearchServiceGrpc;
import com.example.mutualfund.grpc.MutualFundSearchResultGrpc;
import com.example.mutualfund.grpc.SearchGrpcRequest;
import com.example.worker.service.LocalSearchService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MutualFundSearchGrpcResource extends MutinyMutualFundSearchServiceGrpc.MutualFundSearchServiceImplBase {

    @Inject
    LocalSearchService searchService;

    @Override
    @Blocking
    public Multi<MutualFundSearchResultGrpc> searchForMutualFund(SearchGrpcRequest request) {
        return searchService.searchByTag(request.getSearchString());
    }

    /*public long getSizeOfSearchableMutualFund() {
        return searchService.count();
    }*/
}
