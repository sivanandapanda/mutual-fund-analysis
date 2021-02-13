package com.example.common.model;

import java.io.Serializable;
import java.util.Objects;

public class SearchableMutualFund implements Serializable {
    private Long schemeCode;
    private String schemeName;

    public SearchableMutualFund setSchemeCode(Long schemeCode) {
        this.schemeCode = schemeCode;
        return this;
    }

    public SearchableMutualFund setSchemeName(String schemeName) {
        this.schemeName = schemeName;
        return this;
    }

    public Long getSchemeCode() {
        return schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchableMutualFund that = (SearchableMutualFund) o;
        return Objects.equals(schemeCode, that.schemeCode) && Objects.equals(schemeName, that.schemeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemeCode, schemeName);
    }
}
