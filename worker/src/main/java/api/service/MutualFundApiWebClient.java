package api.service;

import api.model.MfApiResponse;
import com.example.common.model.SearchableMutualFund;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@RegisterRestClient(configKey="mf-api")
public interface MutualFundApiWebClient {

    @GET
    @Path("{schemeCode}")
    MfApiResponse getMfData(@PathParam long schemeCode);

    @GET
    List<SearchableMutualFund> getAllMfMetaData();

}
