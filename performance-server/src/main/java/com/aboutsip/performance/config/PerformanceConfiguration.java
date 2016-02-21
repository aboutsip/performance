package com.aboutsip.performance.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import io.dropwizard.Configuration;

/**
 */
public class PerformanceConfiguration extends Configuration{

    @JsonProperty
    @NotNull
    private SIPpConfiguration sipp = new SIPpConfiguration();

    public SIPpConfiguration getSippConfiguration() {
        return sipp;
    }

    public void setSippConfiguration(final SIPpConfiguration sipp) {
        this.sipp = sipp;
    }
}
