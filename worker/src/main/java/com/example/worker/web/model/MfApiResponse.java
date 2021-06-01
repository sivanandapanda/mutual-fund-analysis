package com.example.worker.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MfApiResponse {

    @JsonProperty("meta")
    private Meta mfMetaData;

    @JsonProperty("data")
    private List<NavData> navDataList;

    public static class Meta {
        @JsonProperty("fund_house")
        private String fundHouse;
        @JsonProperty("scheme_type")
        private String schemeType;
        @JsonProperty("scheme_category")
        private String schemeCategory;
        @JsonProperty("scheme_code")
        private Long schemeCode;
        @JsonProperty("scheme_name")
        private String schemeName;

        public String getFundHouse() {
            return fundHouse;
        }

        public Meta setFundHouse(String fundHouse) {
            this.fundHouse = fundHouse;
            return this;
        }

        public String getSchemeType() {
            return schemeType;
        }

        public Meta setSchemeType(String schemeType) {
            this.schemeType = schemeType;
            return this;
        }

        public String getSchemeCategory() {
            return schemeCategory;
        }

        public Meta setSchemeCategory(String schemeCategory) {
            this.schemeCategory = schemeCategory;
            return this;
        }

        public Long getSchemeCode() {
            return schemeCode;
        }

        public Meta setSchemeCode(Long schemeCode) {
            this.schemeCode = schemeCode;
            return this;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public Meta setSchemeName(String schemeName) {
            this.schemeName = schemeName;
            return this;
        }
    }

    public static class NavData {
        private String date;
        private String nav;

        public String getDate() {
            return date;
        }

        public NavData setDate(String date) {
            this.date = date;
            return this;
        }

        public String getNav() {
            return nav;
        }

        public NavData setNav(String nav) {
            this.nav = nav;
            return this;
        }
    }

    public Meta getMfMetaData() {
        return mfMetaData;
    }

    public MfApiResponse setMfMetaData(Meta mfMetaData) {
        this.mfMetaData = mfMetaData;
        return this;
    }

    public List<NavData> getNavDataList() {
        return navDataList;
    }

    public MfApiResponse setNavDataList(List<NavData> navDataList) {
        this.navDataList = navDataList;
        return this;
    }
}