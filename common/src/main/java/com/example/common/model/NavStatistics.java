package com.example.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class NavStatistics implements Serializable {
    private long days;
    private LocalDate date;
    private BigDecimal nav;
    private TenorEnum tenor;
    private Move move;

    public NavStatistics() {}

    public NavStatistics(long days, LocalDate date, BigDecimal nav, TenorEnum tenor, Move move) {
        this.days = days;
        this.date = date;
        this.nav = nav;
        this.tenor = tenor;
        this.move = move;
    }

    public long getDays() {
        return days;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getNav() {
        return nav;
    }

    public TenorEnum getTenor() {
        return tenor;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavStatistics that = (NavStatistics) o;
        return days == that.days && Objects.equals(date, that.date) && Objects.equals(nav, that.nav) && tenor == that.tenor && move == that.move;
    }

    @Override
    public int hashCode() {
        return Objects.hash(days, date, nav, tenor, move);
    }
}
