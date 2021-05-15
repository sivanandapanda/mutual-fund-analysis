package api.resource;

import api.service.MutualFundService;
import com.example.common.model.MutualFund;
import com.example.common.model.MutualFundMeta;
import com.example.common.model.MutualFundStatistics;
import com.example.common.model.SearchableMutualFund;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@Path("mf")
public class MutualFundResource {

    @Inject
    MutualFundService mutualFundService;

    @GET
    @Path("list")
    @Counted(name = "getAllMutualFunds")
    @Timed(name = "getAllMutualFundsTimer")
    public List<MutualFundMeta> getAllMutualFunds() {
        return mutualFundService.getAllMutualFunds();
    }

    @GET
    @Path("{schemeCode}")
    @Counted(name = "getMutualFund")
    @Timed(name = "getMutualFundTimer")
    public MutualFund getMutualFund(@PathParam("schemeCode") long schemeCode){
        return mutualFundService.retrieveMutualFundNav(schemeCode);
    }

    @GET
    @Path("statistics/{schemeCode}")
    @Timed(name = "getMutualFundStatisticsTimer")
    @Counted(name = "getMutualFundStatistics")
    public MutualFundStatistics getMutualFundStatistics(@PathParam("schemeCode") long schemeCode){
        return mutualFundService.getStatistics(schemeCode);
    }

    @GET
    @Path("searchSize")
    @Timed(name = "getSizeOfSearchableMutualFundTimer")
    @Counted(name = "getSizeOfSearchableMutualFund")
    public long getSizeOfSearchableMutualFund(){
        return mutualFundService.getSizeOfSearchableMutualFund();
    }

    @GET
    @Path("search")
    @Timed(name = "searchForMutualFundTimer")
    @Counted(name = "searchForMutualFund")
    public List<SearchableMutualFund> searchForMutualFund(@QueryParam("schemeName") String schemeName, @QueryParam("page") int page,
                                                          @QueryParam("size") int size) {
        if(page <= 0) {
            page = 1;
        }

        if(size <= 0) {
            size = 25;
        }
        return mutualFundService.searchForMutualFund(schemeName, page, size);
    }

    @DELETE
    @Path("cache/clear")
    @Timed(name = "clearCacheTimer")
    @Counted(name = "clearCache")
    public void clearCache(){
        mutualFundService.clearCache();
    }
}
