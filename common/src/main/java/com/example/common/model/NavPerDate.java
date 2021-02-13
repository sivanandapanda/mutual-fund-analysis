package com.example.common.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class NavPerDate implements Serializable {
    private Long schemeCode;
    private LocalDate date;
    private BigDecimal nav;

    public NavPerDate() {}

    public NavPerDate(Long schemeCode, LocalDate date, BigDecimal nav) {
        this.schemeCode = schemeCode;
        this.date = date;
        this.nav = nav;
    }

    public Long getSchemeCode() {
        return schemeCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getNav() {
        return nav;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavPerDate that = (NavPerDate) o;
        return Objects.equals(schemeCode, that.schemeCode) && Objects.equals(date, that.date) && Objects.equals(nav, that.nav);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemeCode, date, nav);
    }
}

