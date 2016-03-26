package com.aboutsip.performance.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

/**
 *
 */
public class SIPpInstanceConfig {

    /**
     * The path to the folder containing the scenario file (if a file is used)
     */
    @JsonProperty
    private File scenarioFolder;

    /**
     * The scenario to execute, which may be the name of a built in SIPp scenario
     * or the name of a scenario file. If it is a scenario file it is expected that
     * the scenario ends with ".xml" (simply but works)
     */
    @JsonProperty
    private String scenario;

    /**
     * The listening address that the SIPp instance should bind to. Also note that if you
     * start a scenario on a server that does not have this IP it will try and start the
     * SIPp instance on a remote controller using the REST interface.
     */
    @JsonProperty
    private String host;

    /**
     * The port that the SIPp instance should bind to
     */
    @JsonProperty
    private int port;

    /**
     * If the scenario requires it, i.e. the sipp instance is acting as a UAC, you must
     * specify the remote host to which we are to send the traffic.
     */
    @JsonProperty
    private String remoteHost;

    /**
     * See {@link SIPpInstanceConfig#remoteHost}
     */
    @JsonProperty
    private int remotePort;

    public File getScenarioFolder() {
        return scenarioFolder;
    }

    public String getScenario() {
        return scenario;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }
}
