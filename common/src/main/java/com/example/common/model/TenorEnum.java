package com.example.common.model;

public enum TenorEnum {
    ONED(0), THREED(1), ONEW(2), TWOW(3), ONEM(4), TWOM(5), SIXM(6),
    ONEY(7), TWOY(8), FIVEY(9), INCEPTION(10);

    private int tenorValue;

    TenorEnum(int tenorValue) {
        this.tenorValue = tenorValue;
    }

    public int getTenorValue() {
        return tenorValue;
    }
}
