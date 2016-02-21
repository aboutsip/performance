package com.aboutsip.performance.api.sipp;

import com.aboutsip.performance.core.sipp.SIPp;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

/**
 * A transfer object representing a running instance of SIPp (well, or
 * at least an instance that was running at some point. It may just
 * have died)
 *
 */
public class SIPpTO {

    private SIPpTO() {

    }

    public static SIPpTO create(final SIPp instance) {
        return null;
    }
}
