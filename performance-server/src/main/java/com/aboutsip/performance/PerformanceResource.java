package com.aboutsip.performance;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

/**
 * Defines the main REST interface to the performance server.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class PerformanceResource {

    public PerformanceResource() {
    }

}
