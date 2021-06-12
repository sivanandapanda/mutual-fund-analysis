package com.example.web.resource;

import com.example.common.model.SearchableMutualFund;
import com.example.web.model.Dashboard;
import com.example.web.service.MutualFundGrpcService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.resteasy.annotations.GZIP;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("mf/api/v1")
public class MutualFundWebResource {

    @Inject
    MutualFundGrpcService service;

    @GET
    @Path("dashboard/create")
    @Counted(name = "createDashboard")
    @Timed(name = "createDashboardTimer")
    public Uni<List<Dashboard>> createDashboard(@QueryParam("schemeCodes") String schemeCodes) {
        List<Long> schemeCodeLongList = Arrays.stream(schemeCodes.split(",")).map(Long::parseLong).collect(Collectors.toList());

        return service.getDashBoardFrom(schemeCodeLongList);
    }

    @GET
    @Path("mutualfund/search")
    @Counted(name = "searchMutualFunds")
    @Timed(name = "searchMutualFundsTimer")
    public Uni<List<SearchableMutualFund>> searchMutualFunds(@QueryParam("schemeName") String schemeName) {
        return service.searchMutualFunds(schemeName);
    }

    @GET
    @GZIP
    @Path("mutualfund/explore")
    @Counted(name = "getMutualFund")
    @Timed(name = "exploreMutualFundsTimer")
    public Uni<List<Dashboard>> exploreMutualFunds(@QueryParam("schemeName") String schemeName, @QueryParam("sampleSize") int sampleSize) {
        if(sampleSize <= 0) {
            sampleSize = 10;
        }
        return service.exploreMutualFunds(schemeName, sampleSize);
    }
}
