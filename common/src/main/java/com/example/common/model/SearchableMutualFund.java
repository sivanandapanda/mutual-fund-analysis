package com.example.common.model;

import java.io.Serializable;
import java.util.Objects;

public class SearchableMutualFund implements Serializable {
    private Long schemeCode;
    private String schemeName;
    private Long searchScore;

    public SearchableMutualFund(Long schemeCode, String schemeName, Long searchScore) {
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

    public Long getSearchScore() {
        return searchScore;
    }

    public void setSearchScore(Long searchScore) {
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
