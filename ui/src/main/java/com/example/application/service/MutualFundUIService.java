package com.example.application.service;

import com.example.common.model.Dashboard;
import com.example.common.model.SearchableMutualFund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Collections.emptyList;

@Service
public class MutualFundUIService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mf.web.server.url}")
    private String webServerUrl;

    public List<Dashboard> loadDashboard(String favMfSchemeCodes) {
        var url = webServerUrl + "/api/v1/mf/dashboard/create?schemeCodes="+favMfSchemeCodes;
        var dashboards = restTemplate.getForObject(url, Dashboard[].class);
        return dashboards != null ? List.of(dashboards) : emptyList();
    }

    public List<Dashboard> explore(String searchString, String sampleSize) {
        var url = webServerUrl + "/api/v1/mf/explore?schemeName=" + searchString;
        if(sampleSize != null && !sampleSize.isEmpty()) {
            url += "&sampleSize="+sampleSize;
        }
        var dashboards = restTemplate.getForObject(url, Dashboard[].class);
        return dashboards != null ? List.of(dashboards) : emptyList();
    }

    public List<SearchableMutualFund> search(String searchString) {
        var url = webServerUrl + "/api/v1/mf/search?schemeName=" + searchString;
        var results = restTemplate.getForObject(url, SearchableMutualFund[].class);
        return results != null ? List.of(results) : emptyList();
    }
}
