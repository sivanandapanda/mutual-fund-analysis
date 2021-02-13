package com.example.common.model;

import java.io.Serializable;
import java.util.List;

public class Statistics implements Serializable {

    private List<NavStatistics> statisticsList;

    public Statistics() {}

    public Statistics(List<NavStatistics> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public List<NavStatistics> getStatisticsList() {
        return statisticsList;
    }
}
