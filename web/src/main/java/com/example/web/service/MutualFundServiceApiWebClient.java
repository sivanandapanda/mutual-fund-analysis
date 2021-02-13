package com.example.web.service;

import com.example.common.model.MutualFundStatistics;
import com.example.common.model.SearchableMutualFund;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@RegisterRestClient(configKey="mf-service-api")
public interface MutualFundServiceApiWebClient {

    @GET
    @Path("/mf/statistics/{schemeCode}")
    MutualFundStatistics getMfData(@PathParam long schemeCode);

    @GET
    @Path("/mf/search")
    List<SearchableMutualFund> searchMutualFunds(@QueryParam String schemeName, @QueryParam int size);

}
