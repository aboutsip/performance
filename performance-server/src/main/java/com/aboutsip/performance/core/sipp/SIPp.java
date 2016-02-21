package com.aboutsip.performance.core.sipp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SIPp {

    @JsonProperty
    UUID getUUID();

    @JsonProperty
    String getFriendlyName();

    /**
     * Start this instance.
     *
     * @return
     * @throws IllegalStateException in case this instance is dead, i.e.
     * it was running but has since then been stopped.
     */
    CompletableFuture<SIPp> start() throws IllegalStateException;

    /**
     * Stop this instance, forcefully if necessary.
     *
     * If the instance already has been stopped then the request to stop
     * it again is silently ignored and a successful {@link CompletableFuture}
     * is returned.
     *
     * @return
     * @throws IllegalStateException in case this instance has never been started.
     */
    CompletableFuture<SIPp> stop(boolean force) throws IllegalStateException;

    /**
     *
     * @return
     */
    CompletableFuture<SIPp> pause();

    enum Type {
        UAC, UAS;
    }

    interface Builder {
        Builder withFriendlyName(String name);

        /**
         * The name of the scenario or scenario file. If the name
         * is 'uas' or 'uac' it is assumed it is the built in scenarios,
         * else it is assumed this is the name of a scenario file.
         *
         * @param scenario
         * @return
         */
        Builder withScenario(String scenario);

        Builder withListenAddress(String address);

        /**
         * The local listening port. If not specified, any open port
         * will be selected.
         *
         * @param port
         * @return
         */
        Builder withListenPort(int port);

        /**
         * For
         * @param host
         * @return
         */
        Builder withRemoteHost(String host);
        Builder withRemotePort(int port);
        SIPp build();
    }
}
