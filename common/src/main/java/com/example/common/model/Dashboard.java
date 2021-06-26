package com.example.common.model;

import java.io.Serializable;

public class Dashboard implements Serializable {
    private MutualFundStatistics mutualFundStatistics;

    public Dashboard() {}

    public Dashboard(MutualFundStatistics mutualFundStatistics) {
        this.mutualFundStatistics = mutualFundStatistics;
    }

    public MutualFundStatistics getMutualFundStatistics() {
        return mutualFundStatistics;
    }
}
