package com.bluenet.web.api.dto.audit;

public enum StatisticsPeriod {
    H24("24h"),
    D7("7d"),
    D30("30d");

    private final String value;

    StatisticsPeriod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StatisticsPeriod fromValue(String value) {
        for (StatisticsPeriod period : values()) {
            if (period.value.equalsIgnoreCase(value)) {
                return period;
            }
        }
        return D7;
    }
}
