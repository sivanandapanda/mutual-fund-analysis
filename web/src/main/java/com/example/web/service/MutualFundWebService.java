package com.example.web.service;

import com.example.common.model.SearchableMutualFund;
import com.example.web.model.Dashboard;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MutualFundWebService {

    @Inject
    @RestClient
    MutualFundServiceApiWebClient webClient;

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



        return webClient.searchMutualFunds(schemeName, sampleSize)
                //.stream()
                .parallelStream()
                .map(searchableMutualFund -> webClient.getMfData(searchableMutualFund.getSchemeCode()))
                .map(Dashboard::new)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getPercentageIncrease().compareTo(d1.getMutualFundStatistics().getPercentageIncrease()))
                .limit(100)
                .collect(Collectors.toList());
    }
}
