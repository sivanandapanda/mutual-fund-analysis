package com.example.common.model;


import com.example.mutualfund.grpc.MutualFundMetaGrpc;

import java.io.Serializable;
import java.util.Objects;

public class MutualFundMeta implements Serializable {

    private String fundHouse;
    private String schemeType;
    private String schemeCategory;
    private Long schemeCode;
    private String schemeName;

    public String getFundHouse() {
        return fundHouse;
    }

    public MutualFundMeta setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
        return this;
    }

    public String getSchemeType() {
        return schemeType;
    }

    public MutualFundMeta setSchemeType(String schemeType) {
        this.schemeType = schemeType;
        return this;
    }

    public String getSchemeCategory() {
        return schemeCategory;
    }

    public MutualFundMeta setSchemeCategory(String schemeCategory) {
        this.schemeCategory = schemeCategory;
        return this;
    }

    public Long getSchemeCode() {
        return schemeCode;
    }

    public MutualFundMeta setSchemeCode(Long schemeCode) {
        this.schemeCode = schemeCode;
        return this;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MutualFundMeta setSchemeName(String schemeName) {
        this.schemeName = schemeName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutualFundMeta that = (MutualFundMeta) o;
        return Objects.equals(fundHouse, that.fundHouse) && Objects.equals(schemeType, that.schemeType)
                && Objects.equals(schemeCategory, that.schemeCategory) && Objects.equals(schemeCode, that.schemeCode) && Objects.equals(schemeName, that.schemeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundHouse, schemeType, schemeCategory, schemeCode, schemeName);
    }

    public MutualFundMetaGrpc convertToGrpcModel() {
        return MutualFundMetaGrpc.newBuilder()
                .setFundHouse(this.getFundHouse())
                .setSchemeCategory(this.getSchemeCategory())
                .setSchemeType(this.getSchemeType())
                .setSchemeName(this.getSchemeName())
                .setSchemeCode(this.getSchemeCode())
                .build();
    }

    public static MutualFundMeta convertFromGrpcModel(MutualFundMetaGrpc m) {
        var mutualFundMeta = new MutualFundMeta();
        mutualFundMeta.setFundHouse(m.getFundHouse());
        mutualFundMeta.setSchemeCategory(m.getSchemeCategory());
        mutualFundMeta.setSchemeCode(m.getSchemeCode());
        mutualFundMeta.setSchemeName(m.getSchemeName());
        mutualFundMeta.setSchemeType(m.getSchemeType());
        return mutualFundMeta;
    }
}
