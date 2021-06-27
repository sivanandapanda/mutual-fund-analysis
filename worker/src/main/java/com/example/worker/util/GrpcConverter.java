package com.example.worker.util;

import com.example.common.model.MutualFund;
import com.example.common.model.MutualFundMeta;
import com.example.common.model.NavPerDate;
import com.example.mutualfund.grpc.MutualFundGrpc;
import com.example.mutualfund.grpc.MutualFundMetaGrpc;
import com.example.mutualfund.grpc.MutualFundSearchResultGrpc;
import com.example.mutualfund.grpc.NavPerDateGrpc;
import com.example.worker.web.model.MfMetaData;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public final class GrpcConverter {

    private GrpcConverter() {}

    public static MutualFundGrpc convertToGrpcModel(MutualFund mutualFund) {
        return MutualFundGrpc.newBuilder()
                .setMeta(convertToGrpcModel(mutualFund.getMeta()))
                .addAllNavPerDates(mutualFund.getNavPerDates().stream().map(GrpcConverter::convertToGrpcModel).collect(Collectors.toList()))
                .build();
    }

    public static MutualFundMetaGrpc convertToGrpcModel(MutualFundMeta mutualFundMeta) {
        return MutualFundMetaGrpc.newBuilder()
                .setFundHouse(mutualFundMeta.getFundHouse())
                .setSchemeCategory(mutualFundMeta.getSchemeCategory())
                .setSchemeType(mutualFundMeta.getSchemeType())
                .setSchemeName(mutualFundMeta.getSchemeName())
                .setSchemeCode(mutualFundMeta.getSchemeCode())
                .build();
    }

    public static NavPerDateGrpc convertToGrpcModel(NavPerDate navPerDate) {
        return NavPerDateGrpc.newBuilder()
                .setDate(navPerDate.getDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                .setSchemeCode(navPerDate.getSchemeCode())
                .setNav(navPerDate.getNav().doubleValue())
                .build();
    }

    public static MutualFundSearchResultGrpc convertToGrpcModel(MfMetaData mfMetaData) {
        return MutualFundSearchResultGrpc.newBuilder()
                .setSchemeCode(mfMetaData.getSchemeCode())
                .setSchemeName(mfMetaData.getSchemeName())
                .build();
    }
}
