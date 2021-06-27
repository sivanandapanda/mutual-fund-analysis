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

    public List<Dashboard> loadDashboard() {
        var url = webServerUrl + "/api/v1/mf/dashboard/create?schemeCodes=141245,141244,141243,141246,141463,141466,141465,141464,141701,141700,141699,141698,139862,139861,139859,139860,140065,140068,140067,140066,143088,143085,143087,143086,135304,135302,135303,135301,135358,135359,135357,135360,135512,135513,135510,135511,135861,135863,135862,135860,136123,136124,136121,136122,136418,136419,136417,136420,140654,140655,140657,140656,124595,124596,106338,106337,125077,106709,106707,125078,106706,126030";
        //var url = webServerUrl + "/api/v1/mf/dashboard/create?schemeCodes=141245,141244";
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
