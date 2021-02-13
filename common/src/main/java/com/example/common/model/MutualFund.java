package com.example.common.model;

import java.io.Serializable;
import java.util.List;

public class MutualFund implements Serializable {

    private MutualFundMeta meta;
    private List<NavPerDate> navPerDates;

    public MutualFundMeta getMeta() {
        return meta;
    }

    public MutualFund setMeta(MutualFundMeta meta) {
        this.meta = meta;
        return this;
    }

    public List<NavPerDate> getNavPerDates() {
        return navPerDates;
    }

    public MutualFund setNavPerDates(List<NavPerDate> navPerDates) {
        this.navPerDates = navPerDates;
        return this;
    }

    @Override
    public String toString() {
        return "MutualFund{" +
                ", meta=" + meta +
                ", navPerDates=" + navPerDates +
                '}';
    }
}
