package com.aboutsip.performance.core.sipp;

import com.aboutsip.performance.core.sipp.impl.DefaultSIPp;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * You can't have a Java project without a manager!
 *
 */
public final class SIPpManager {

    private final Map<UUID, SIPp> instances;
    private final ScheduledExecutorService executorService;

    public SIPpManager() {
        this(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2));
    }

    public SIPpManager(final ScheduledExecutorService executorService) {
        this.executorService = executorService;
        this.instances = new ConcurrentHashMap<>();
    }

    public List<SIPp> getAllInstances() {
        return instances.values().stream().collect(Collectors.toList());
    }

    public Optional<SIPp> getInstance(final UUID uuid) {
        return Optional.ofNullable(instances.get(uuid));
    }

    public SIPp.Builder newInstance() {
        final UUID uuid = UUID.randomUUID();
        return new SippBuilder(uuid);
    }

    private class SippBuilder implements SIPp.Builder {

        private final UUID uuid;
        private String friendlyName;

        private String scenario;

        private String remoteHost;
        private int remotePort = 5060;

        public SippBuilder(final UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public SIPp.Builder withFriendlyName(final String name) {
            this.friendlyName = name;
            return this;
        }

        @Override
        public SIPp.Builder withScenario(final String scenario) {
            this.scenario = scenario;
            return this;
        }

        @Override
        public SIPp.Builder withListenAddress(final String address) {
            return this;
        }

        @Override
        public SIPp.Builder withListenPort(final int port) {
            return this;
        }

        @Override
        public SIPp.Builder withRemoteHost(final String host) {
            if (host == null || host.isEmpty()) {
                remoteHost = null;
            } else {
                remoteHost = host;
            }
            return this;
        }

        @Override
        public SIPp.Builder withRemotePort(final int port) {
            Preconditions.checkArgument(port > 0, "You must specify a valid port");
            this.remotePort = port;
            return this;
        }

        @Override
        public SIPp build() {
            // final ProcessBuilder builder = new ProcessBuilder("sipp", "-sn");
            final List<String> args = new ArrayList<>();
            args.add("sipp");

            final String baseName = configureScenario(args);
            configureStatsOptions(args);
            configureRemoteHostOptions(args);
            final ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);

            final SIPp sipp = new DefaultSIPp(executorService, builder, uuid, baseName, friendlyName);
            final SIPp current = instances.putIfAbsent(uuid, sipp);
            if (current != null) {
                throw new IllegalStateException("There was already another SIPp instance with uuid " + uuid);
            }
            return sipp;
        }

        private void configureRemoteHostOptions(final List<String> args) {
            // TODO: should really try and figure out if we need remote options
            // based on the type of scenario...
            if (remoteHost != null) {
                args.add(remoteHost + ":" + remotePort);
            }
        }

        /**
         * Figure out what scenario, or scenario file, we are running and add the appropriate
         * arguments. Also, since the name of the scenario is used as a sort of 'base name' for
         * naming the various files SIPp spits out, let's return that as well because it is needed
         * when we try and figure out what files to tail etc.
         *
         * @param args
         */
        private String configureScenario(final List<String> args) {
            final String finalScenario = scenario == null || scenario.isEmpty() ? "uas" : scenario;
            final String baseName;
            if ("uas".equalsIgnoreCase(finalScenario) || "uac".equalsIgnoreCase(finalScenario)) {
                args.add("-sn");
                baseName = finalScenario;
            } else {
                args.add("-sf");
                // not correct. Will deal with that later
                baseName = finalScenario;
            }
            args.add(finalScenario);
            return baseName;
        }

        /**
         * We need to always run with all the stats turned on because otherwise
         * it is just too hard (and annoying) to get all the stats out of SIPp.
         *
         * @param args
         */
        private void configureStatsOptions(final List<String> args) {
            args.add("-trace_stat");
            args.add("-fd");
            args.add("1");
            args.add("-trace_counts");
        }

    }

}
