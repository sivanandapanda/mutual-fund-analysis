package com.example.web.model;

import com.example.common.model.MutualFundStatistics;

public class Dashboard {
    private MutualFundStatistics mutualFundStatistics;

    public Dashboard() {}

    public Dashboard(MutualFundStatistics mutualFundStatistics) {
        this.mutualFundStatistics = mutualFundStatistics;
    }

    public MutualFundStatistics getMutualFundStatistics() {
        return mutualFundStatistics;
    }
}
