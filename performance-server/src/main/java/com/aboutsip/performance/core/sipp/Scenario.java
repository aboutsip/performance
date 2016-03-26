package com.aboutsip.performance.core.sipp;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@link Scenario} represents a sipp scenario file but with added description
 * and comments etc.
 */
public class Scenario {

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;
}
