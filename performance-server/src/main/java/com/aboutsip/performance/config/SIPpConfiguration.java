package com.aboutsip.performance.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main configuration class for all things related to managing SIPp instances.
 *
 */
public class SIPpConfiguration {

    @JsonProperty
    private String executable;

}
