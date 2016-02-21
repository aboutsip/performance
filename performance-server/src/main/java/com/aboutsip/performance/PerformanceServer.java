package com.aboutsip.performance;

import com.aboutsip.performance.config.PerformanceConfiguration;
import com.aboutsip.performance.core.sipp.SIPpManager;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 *
 */
public class PerformanceServer extends Application<PerformanceConfiguration> {

    private SIPpManager sippManager;

    @Override
    public void initialize(final Bootstrap<PerformanceConfiguration> bootstrap) {
        this.sippManager = new SIPpManager();
    }

    @Override
    public void run(final PerformanceConfiguration configuration, final Environment environment) throws Exception {
        final SIPpResource sippResource = new SIPpResource(sippManager);
        environment.jersey().register(sippResource);
    }

    public static void main(final String[] args) throws Exception {
        new PerformanceServer().run(args);
    }

}
