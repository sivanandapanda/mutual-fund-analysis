package com.example.web.service;

import com.example.common.model.MutualFundStatistics;
import com.example.common.model.SearchableMutualFund;
import com.example.web.model.Dashboard;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@ApplicationScoped
public class MutualFundWebService {

    @Inject
    Logger log;

    @Inject
    @RestClient
    MutualFundServiceApiWebClient webClient;

    @ConfigProperty(name = "web.threadPool.size")
    int threadPoolSize;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<Dashboard> getDashBoardFrom(List<Long> schemeCodes) {
        return schemeCodes.stream()
                .map(schemeCode -> webClient.getMfData(schemeCode))
                .map(Dashboard::new)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getMutualFundMeta().getSchemeCode().compareTo(d1.getMutualFundStatistics().getMutualFundMeta().getSchemeCode()))
                .collect(Collectors.toList());
    }

    public List<SearchableMutualFund> searchMutualFunds(String schemeName) {
        return webClient.searchMutualFunds(schemeName, 0);
    }

    public List<Dashboard> exploreMutualFunds(String schemeName, int sampleSize) {
        List<Future<MutualFundStatistics>> futures = webClient.searchMutualFunds(schemeName, sampleSize)
                .parallelStream()
                .map(searchableMutualFund -> executorService.submit(() -> webClient.getMfData(searchableMutualFund.getSchemeCode())))
                .collect(Collectors.toList());

        return futures.parallelStream()
                .map(f -> {
                    try {
                        return new Dashboard(f.get());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Exception occurred while retrieving result from future", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getPercentageIncrease().compareTo(d1.getMutualFundStatistics().getPercentageIncrease()))
                .limit(100)
                .sorted(Comparator.comparing(d -> d.getMutualFundStatistics().getPercentageIncrease()))
                .collect(Collectors.toList());
    }

    @PreDestroy
    public void destroy() {
        if(Objects.nonNull(executorService)) {
            executorService.shutdownNow();
        }
    }
}
