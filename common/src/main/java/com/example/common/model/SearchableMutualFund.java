package com.example.common.model;

import java.io.Serializable;
import java.util.Objects;

public class SearchableMutualFund implements Serializable {
    private Long schemeCode;
    private String schemeName;
    private Double searchScore;

    public SearchableMutualFund(Long schemeCode, String schemeName, Double searchScore) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
        this.searchScore = searchScore;
    }

    public SearchableMutualFund() {}

    public Long getSchemeCode() {
        return schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public Double getSearchScore() {
        return searchScore == null ? 0 : searchScore;
    }

    public void setSearchScore(Double searchScore) {
        this.searchScore = searchScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchableMutualFund that = (SearchableMutualFund) o;
        return Objects.equals(schemeCode, that.schemeCode) && Objects.equals(schemeName, that.schemeName)
                && Objects.equals(searchScore, that.searchScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemeCode, schemeName, searchScore);
    }
}
