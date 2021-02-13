package com.example.common.model;

import java.io.Serializable;

public class MutualFundStatistics implements Serializable {
    private MutualFundMeta mutualFundMeta;
    private Statistics statistics;
    private Double percentageIncrease;

    public MutualFundStatistics() {}

    public MutualFundStatistics(MutualFundMeta mutualFundMeta, Statistics statistics, Double percentageIncrease) {
        this.mutualFundMeta = mutualFundMeta;
        this.statistics = statistics;
        this.percentageIncrease = percentageIncrease;
    }

    public MutualFundMeta getMutualFundMeta() {
        return mutualFundMeta;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Double getPercentageIncrease() {
        return percentageIncrease;
    }
}
