package com.aboutsip.performance.core.sipp.impl;

import com.aboutsip.performance.core.sipp.SIPp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Represents a SIPp "task". I.e. you wish to run a particular
 * scenario for x hours and the job of this class is to ensure
 * that this indeed happens. The actual SIPp process has a tendency
 * to crash sometimes so we want to be able to just create a new
 * instance and continue like nothing happened. As such, you could
 * think of the {@link DefaultSIPp} as your intent and then there may
 * be a need to kick off multiple {@link SIPpInstance}s to make that
 * happen (which the user shouldn't worry about)
 */
public final class DefaultSIPp implements SIPp {

    private final Logger logger = LoggerFactory.getLogger(DefaultSIPp.class);

    private final UUID uuid;
    private final String friendlyName;

    /**
     * Whenever we need to start a new sipp instance, perhaps because the
     * previous one crashed, we will just use this process builder that
     * will kick off an identical process to the previous one (or the very
     * first one of course)
     */
    private final ProcessBuilder processBuilder;

    private final ScheduledExecutorService executorService;

    /**
     * The name is very important because we are reading stats from files and those
     * files are based on the scenario name and pid. Would be nice if one could have
     * configured this but it is what it is.
     */
    private final String name;

    private final Object lock = new Object();
    private SIPpInstance sippInstance;

    /**
     * Need to remember this future if several threads are calling start at the same
     * time or after a sipp instance already has been created and is running.
     */
    private CompletableFuture<SIPp> currentStartFuture;


    public DefaultSIPp(final ScheduledExecutorService executorService,
                       final ProcessBuilder processBuilder,
                       final UUID uuid,
                       final String name,
                       final String friendlyName) {
        this.executorService = executorService;
        this.processBuilder = processBuilder;
        this.uuid = uuid;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getFriendlyName() {
        return this.friendlyName;
    }

    @Override
    public int getTargetRate() {
        synchronized (sippInstance) {
            if (sippInstance != null) {
                return sippInstance.getTargetRate();
            }
            return -1;
        }
    }

    @Override
    public double getCurrentRate() {
        synchronized (sippInstance) {
            if (sippInstance != null) {
                return sippInstance.getCurrentRate();
            }
            return -1;
        }
    }

    @Override
    public CompletableFuture<SIPp> start() throws IllegalStateException {
        synchronized (lock) {
            if (currentStartFuture != null) {
                return currentStartFuture;
            }

            final CompletableFuture<SIPpInstance> future = SIPpInstance.create(executorService, processBuilder, name);
            currentStartFuture = future.thenApply(instance -> {
                synchronized (lock) {
                    sippInstance = instance;
                }
                return DefaultSIPp.this;
            });

            return currentStartFuture;
        }
    }



    @Override
    public CompletableFuture<SIPp> stop(boolean force) throws IllegalStateException {
        synchronized (lock) {
            if (sippInstance != null) {
                return sippInstance.stop().thenApply(instance -> DefaultSIPp.this);
            } else {
                throw new IllegalStateException("This instance was never started");
            }
        }
    }

    @Override
    public boolean cleanUp() throws IllegalStateException {
        synchronized (lock) {
            if (sippInstance != null) {
                return sippInstance.deleteFiles();
            } else {
                throw new IllegalStateException("This instance was never started");
            }
        }
    }

    @Override
    public CompletableFuture<SIPp> pause() {
        return null;
    }

    @Override
    public CompletableFuture<SIPp> increase10() {
        synchronized (lock) {
            if (sippInstance != null) {
                return sippInstance.increaseRateBy10().thenApply(instance -> DefaultSIPp.this);
            } else {
                throw new IllegalStateException("This instance was never started");
            }
        }
    }

    @Override
    public CompletableFuture<SIPp> decrease10() {
        synchronized (lock) {
            if (sippInstance != null) {
                return sippInstance.decreaseRateBy10().thenApply(instance -> DefaultSIPp.this);
            } else {
                throw new IllegalStateException("This instance was never started");
            }
        }
    }

    @Override
    public CompletableFuture<SIPp> setRate(int rate) {
        synchronized (lock) {
            if (sippInstance != null) {
                return sippInstance.setRate(rate).thenApply(instance -> DefaultSIPp.this);
            } else {
                throw new IllegalStateException("This instance was never started");
            }
        }
    }

}
