package com.example.aivideotransum.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JobAction {
    SUMMARIZE,
    TRANSLATE;

    @JsonCreator
    public static JobAction fromString(String value) {
        return JobAction.valueOf(value.toUpperCase());
    }
}
