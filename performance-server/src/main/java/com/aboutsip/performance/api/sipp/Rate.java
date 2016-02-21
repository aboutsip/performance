package com.aboutsip.performance.api.sipp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the rate of a current sipp instance.
 */
public class Rate {

    @JsonProperty
    private int targetRate;

    @JsonProperty
    private int currentRate;


    public Rate(final int targetRate, final int currentRate) {
        this.targetRate = targetRate;
        this.currentRate = currentRate;
    }

    public int getTargetRate() {
        return targetRate;
    }

    public int getCurrentRate() {
        return currentRate;
    }
}
