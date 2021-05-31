package api.service;

import com.example.mutualfund.grpc.MutinyMutualFundSearchServiceGrpc;
import com.example.mutualfund.grpc.MutualFundSearchResult;
import com.example.mutualfund.grpc.SearchRequest;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Singleton
public class MutualFundSearchGrpcService extends MutinyMutualFundSearchServiceGrpc.MutualFundSearchServiceImplBase {

    @Inject
    Logger log;

    @Inject
    LocalSearchService searchService;

    @Inject
    @RestClient
    MutualFundApiWebClient webClient;

    private static final Lock LOCK = new ReentrantLock();

    @Override
    @Blocking
    public Multi<MutualFundSearchResult> searchForMutualFund(SearchRequest request) {
        log.info("searchForMutualFund " + request.getSearchString());
        try {
            LOCK.lock();
            if (searchService.indexSize() <= 0) {
                searchService.saveAll(webClient.getAllMfMetaData());
            }
        } finally {
            LOCK.unlock();
        }

        var searchableMutualFunds = searchService.searchByTag(request.getSearchString(), request.getPage(), request.getSize());

        var collect = searchableMutualFunds.stream()
                .map(s -> MutualFundSearchResult.newBuilder()
                        .setSchemeCode(s.getSchemeCode())
                        .setSchemeName(s.getSchemeName())
                        .setSearchScore(s.getSearchScore())
                        .build())
                .collect(Collectors.toList());

        return Multi.createFrom().iterable(collect);
    }

    public long getSizeOfSearchableMutualFund() {
        return searchService.count();
    }
}
