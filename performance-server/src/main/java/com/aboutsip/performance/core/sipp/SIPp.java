package com.aboutsip.performance.core.sipp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SIPp {

    @JsonProperty
    UUID getUUID();

    @JsonProperty
    String getFriendlyName();

    /**
     * Get the target rate.
     *
     * @return the target call rate or -1 if the underlying SIPp process
     * isn't running or that we haven't received any stats from it just yet.
     */
    @JsonProperty
    int getTargetRate();

    /**
     * Get the current rate, which may or may not be
     * equal to that of the target rate.
     *
     * @return the current call rate or -1.0 if the underlying SIPp process
     * isn't running or that we haven't received any stats from it just yet.
     */
    @JsonProperty
    double getCurrentRate();

    /**
     * The number of retransmissions that has occurred since last report.
     *
     * @return
     */
    @JsonProperty
    int getRetransmissions();

    @JsonIgnore
    StatsObject getStats();

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
     * Delete any files that was created when running sipp
     * @return
     * @throws IllegalStateException in case the SIPp instance is still running.
     */
    boolean cleanUp() throws IllegalStateException;

    /**
     *
     * @return
     */
    CompletableFuture<SIPp> pause();

    /**
     * Increase the current rate by 10
     *
     * @return
     */
    CompletableFuture<SIPp> increase10();

    /**
     * Decrease the current rate by 10
     *
     * @return
     */
    CompletableFuture<SIPp> decrease10();

    /**
     * Set the rate to the specified value.
     *
     * @param rate
     * @return
     */
    CompletableFuture<SIPp> setRate(int rate);

    enum Type {
        UAC, UAS;
    }

    /**
     * The version of SIPp.
     */
    enum Version {
        THREE_DOT_ZERO,
        THREE_DOT_ONE,
        THREE_DOT_TWO,
        THREE_DOT_THREE,
        THREE_DOT_FOUR;
    }

    interface Builder {
        Builder withFriendlyName(String name);

        /**
         * Set the initial rate. I.e., the rate SIPp will start with.
         *
         * If not specified, the SIPp instance will start with rate 1.
         *
         * @param rate the initial rate. If a value less than 1 is passed in
         *             the initial rate will be set to 1
         * @return
         */
        Builder withInitialRate(int rate);

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
