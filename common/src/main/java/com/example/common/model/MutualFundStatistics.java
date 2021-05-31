package com.example.common.model;

import java.io.Serializable;
import java.util.List;

public class MutualFundStatistics implements Serializable {
    private MutualFundMeta mutualFundMeta;
    private List<NavStatistics> statistics;
    private Double percentageIncrease;

    public MutualFundStatistics() {}

    public MutualFundStatistics(MutualFundMeta mutualFundMeta, List<NavStatistics> statistics, Double percentageIncrease) {
        this.mutualFundMeta = mutualFundMeta;
        this.statistics = statistics;
        this.percentageIncrease = percentageIncrease;
    }

    public MutualFundMeta getMutualFundMeta() {
        return mutualFundMeta;
    }

    public List<NavStatistics> getStatistics() {
        return statistics;
    }

    public Double getPercentageIncrease() {
        return percentageIncrease;
    }
}
