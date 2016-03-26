package com.aboutsip.performance.core.sipp.impl;

import com.aboutsip.performance.core.sipp.SIPp;
import com.aboutsip.performance.core.sipp.StatsLabels;
import com.aboutsip.performance.core.sipp.StatsObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Represents a running SIPp instance.
 *
 * A SIPp instance can crash and there isn't a one-to-one mapping
 * between our {@link SIPp} representation and a running SIPp process per se.
 * Rather, our {@link SIPp} represents what you are trying to accomplish
 * and you don't care if a the underlying SIPp instance crashes, you just
 * want to e.g. run for x hours and you don't necessarily care if SIPp
 * has to be restarted. Hence, the {@link SIPpInstance} contains
 * the information about the currently running SIPp instance
 * and {@link SIPp} represents the task that you are trying to achieve.
 */
public class SIPpInstance {

    private final Logger logger = LoggerFactory.getLogger(SIPpInstance.class);

    private final ScheduledExecutorService executorService;
    private final int pid;
    private final Process process;

    // The re-directed stdout/in/error
    private final BufferedWriter output;
    private final BufferedReader error;

    // Pointers to the various files that sipp opens.
    // We need these to pull stats out of.
    private final BufferedReader countsFileReader;
    private final BufferedReader statsFileReader;

    // Really only need to keep track of the files if
    // the user asks us to delete them at the end of
    // the run.
    private final File countsFile;
    private final File statsFile;

    // Keeps track of the available labels in the statistics file
    private final StatsLabels statsLabels;

    /**
     * Used when quering for stats when we don't have them just yet.
     *
     */
    private final StatsObject emptyStats;

    private int statsIndex = 0;
    private final Map<Integer, StatsObject> stats;

    /**
     * Used as a lock. Nothing fancy needed...
     */
    private final Object lock = new Object();

    private SIPpInstance(final ScheduledExecutorService executorService,
                         final int pid,
                         final Process process,
                         final BufferedWriter output,
                         final BufferedReader error,
                         final BufferedReader countsFileReader,
                         final BufferedReader statsFileReader,
                         final File countsFile,
                         final File statsFile,
                         final StatsLabels statsLabels) {
        this.executorService = executorService;
        this.pid = pid;
        this.process = process;
        this.output = output;
        this.error = error;
        this.countsFileReader = countsFileReader;
        this.statsFileReader = statsFileReader;
        this.countsFile = countsFile;
        this.statsFile = statsFile;
        this.statsLabels = statsLabels;

        final String emptyStat = "2016-02-26      15:13:39:882    1456528419.882808;2016-02-26    15:13:39:882    1456528419.882808;2016-02-26    15:13:39:886    1456528419.886260;00:00:00;00:00:00;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;00:00:00:000;00:00:00:000;00:00:00:000;00:00:00:000;00:00:00:000;00:00:00:000;00:00:00:000;00:00:00:000;;0;0;0;0;0;0;0;0;0;;0;0;0;0;0;0;0;0;";
        this.emptyStats = statsLabels.createNewStats(emptyStat);

        // really just keep track of a few. If someone is asking for more
        // then lets go and fetch that entry off of file again...
         stats = new LinkedHashMap<Integer, StatsObject>() {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<Integer, StatsObject> eldest) {
                return this.size() > 10;
            }
        };
    }

    public static CompletableFuture<SIPpInstance> create(final ScheduledExecutorService executorService,
                                                         final ProcessBuilder processBuilder,
                                                         final String name) {
        final CompletableFuture<SIPpInstance> future = new CompletableFuture<>();

        final Runnable init = new Runnable() {
            @Override
            public void run() {
                try {
                    final Process process = processBuilder.start();
                    final int pid = getPid(process);

                    final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    Thread.sleep(10);
                    if (!process.isAlive()) {
                        error.lines().forEach(l -> System.err.println(l));
                    }

                    final String countsFileName = String.format("%s_%d_counts.csv", name, pid);
                    final String statsFileName = String.format("%s_%d_.csv", name, pid);

                    // Guess I should schedule a task instead of waiting but easy
                    // and is enough for now...
                    final File countsFile = new File(countsFileName);
                    final File statsFile = new File(statsFileName);
                    final BufferedReader countsFileReader = openStatsFile(countsFile);
                    final BufferedReader statsFilereader = openStatsFile(statsFile);

                    // TODO: get this somehow
                    final SIPp.Version version = SIPp.Version.THREE_DOT_THREE;
                    final StatsLabels labels = StatsLabels.create(version, readLine(statsFilereader));

                    final SIPpInstance instance = new SIPpInstance(executorService, pid, process,
                            output, error, countsFileReader, statsFilereader, countsFile, statsFile, labels);

                    future.complete(instance);

                    // kick off the two tasks to continuously read and process the
                    // data from the various stats files.
                    CompletableFuture.supplyAsync(() -> instance.processOutputFromSIPp(statsFilereader, instance::processStatsLine), executorService);
                    CompletableFuture.supplyAsync(() -> instance.processOutputFromSIPp(countsFileReader, instance::processCountsLine), executorService);

                } catch (final IOException | IllegalAccessException e) {
                    e.printStackTrace();
                    future.completeExceptionally(e);
                } catch (final Exception e) {
                    e.printStackTrace();
                    future.completeExceptionally(e);
                }
            }
        };

        executorService.submit(init);
        return future;
    }

    private static String readLine(final BufferedReader reader) throws IOException {
        String line = null;
        int count = 0;
        while (count++ < 10 && null == (line = reader.readLine())) {
            try {
                Thread.sleep(10);
            } catch (final Throwable t) {
                // ignore;
            }
        }

        if (count == 10) {
            throw new IOException("No data in file");
        }

        return line;
    }

    /**
     * Get the target rate.
     *
     * @return the target rate
     */
    public int getTargetRate() {
        return getLatestStats().orElse(emptyStats).getTargetRate();
    }

    /**
     * Get the current rate.
     *
     * @return the current rate
     */
    public double getCurrentRate() {
        return getLatestStats().orElse(emptyStats).getCallRate();
    }

    public int getRetransmissions() {
        return getLatestStats().orElse(emptyStats).getRetransmissions();
    }

    public int getRetransmissionsCumulative() {
        return getLatestStats().orElse(emptyStats).getRetransmissionsCumulative();
    }

    public StatsObject getStats() {
        return getLatestStats().orElse(emptyStats);
    }

    private Optional<StatsObject> getLatestStats() {
        synchronized (lock) {
            if (stats.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(stats.get(statsIndex - 1));
        }

    }

    private SIPpInstance processStatsLine(final String raw) {
        if (raw == null || raw.isEmpty()) {
            return this;
        }

        final StatsObject stats = statsLabels.createNewStats(raw);
        synchronized (lock) {
            this.stats.put(statsIndex++, stats);
        }

        return this;
    }

    private SIPpInstance processCountsLine(final String raw) {
        // System.err.println(raw);
        return this;
    }

    /**
     * SIPp will dump various types of stats to files.
     * @return
     */
    private SIPpInstance processOutputFromSIPp(final BufferedReader reader, final Consumer<String> function) {

        final List<String> lines = readLines(reader);
        lines.forEach(function);

        if (process.isAlive()) {
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    processOutputFromSIPp(reader, function);
                }
            }, 500, TimeUnit.MILLISECONDS);
        }

        return this;
    }

    private List<String> readLines(final BufferedReader reader) {
        final int max = 10;
        final List<String> lines = new ArrayList<>(max);
        try {
            String line = null;
            int linesRead = 0;
            while (linesRead++ < max && null != (line = reader.readLine())) {
                lines.add(line);
            }
        } catch (final IOException e) {
            // ignore
        }
        return lines;
    }

    /**
     * Set the rate to X. This could mean an increase, decrease or if we already are at the
     * desired rate then this would a no-op.
     *
     * @param rate
     * @return
     */
    public CompletableFuture<SIPpInstance> setRate(final int rate) {
        final int diff = rate - getTargetRate();
        if (diff > 0) {
            return CompletableFuture.supplyAsync(() -> increase(diff), executorService);
        } else if (diff < 0) {
            return CompletableFuture.supplyAsync(() -> decrease(diff), executorService);
        }

        return CompletableFuture.completedFuture(this);
    }

    public CompletableFuture<SIPpInstance> pause() {
        return CompletableFuture.supplyAsync(() -> sendCommand("p"), executorService);
    }

    public CompletableFuture<SIPpInstance> increaseRateBy10() {
        return CompletableFuture.supplyAsync(this::increase10, executorService);
    }

    public CompletableFuture<SIPpInstance> decreaseRateBy10() {
        return CompletableFuture.supplyAsync(this::decrease10, executorService);
    }

    private SIPpInstance increase10() {
        return increase(10);
    }

    private SIPpInstance decrease10() {
        return decrease(10);
    }

    private SIPpInstance increase(final int amount) {
        if (amount >= 10) {
            sendCommand("*");
            increase(amount - 10);
        } else if (amount > 0) {
            sendCommand("+");
            increase(amount - 1);
        }

        return this;
    }

    private SIPpInstance decrease(final int amount) {
        if (amount >= 10) {
            sendCommand("/");
            decrease(amount - 10);
        } else if (amount > 0) {
            sendCommand("-");
            decrease(amount - 1);
        }

        return this;
    }

    public boolean deleteFiles() throws IllegalStateException{
        if (process.isAlive()) {
            throw new IllegalStateException("The process is still running. Can't delete the various stats files");
        }

        return countsFile.delete() && statsFile.delete();
    }

    public CompletableFuture<SIPpInstance> stop() {
        return CompletableFuture.supplyAsync(this::initiateStop, executorService)
                .thenApplyAsync(i -> i.killProcess(), executorService)
                .thenApplyAsync(i -> i.cleanUp(), executorService);
    }

    private SIPpInstance initiateStop() {
        System.err.println("Trying to gracefully stop");
        sendCommand("q");
        return this;
    }

    /**
     * Send a commmand to the underlying SIPp process
     * @param command
     */
    private SIPpInstance sendCommand(final String command) {
        try {
            output.write(command);
            output.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Task that tries to stop the currently running sipp process.
     *
     * @return
     */
    private SIPpInstance killProcess() {
        logger.info("Stopping SIPp");

        // allow it to stop by itself and if that doesn't work then we'll kill it
        waitForProcess(process, 500);
        if (process.isAlive()) {
            System.err.println("Process is still alive. Killing");
            waitForProcess(process.destroyForcibly());
        } else {
            System.err.println("Process is already dead");
        }
        return this;
    }

    private void waitForProcess(final Process process) {
        waitForProcess(process, 0);
    }

    private void waitForProcess(final Process process, final long ms) {
        try {
            if (ms > 0) {
                process.waitFor(500, TimeUnit.MILLISECONDS);
            } else {
                process.waitFor();
            }
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    private SIPpInstance cleanUp() {
        close(countsFileReader);
        close(statsFileReader);
        return this;
    }

    private void close(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException e) {
            // ignore
        }
    }

    private static BufferedReader openStatsFile(final File file) throws FileNotFoundException {
        final long MAX_WAIT = 1000;

        final long ts = System.currentTimeMillis();
        while (!file.exists() && System.currentTimeMillis() - ts < MAX_WAIT) {
            try {
                // use the executor service instead...
                Thread.sleep(20);
            } catch (final Throwable t) {
                // ignore
            }
        }

        try {
            return new BufferedReader(new FileReader(file));
        } catch (final FileNotFoundException e) {
            throw new FileNotFoundException("Unable to open one of the statistics files, needed to keep track of the "
                    + "statistics. The expected file is \"" + file + "\"");
        }
    }

    public static int getPid(final Process process) throws IllegalArgumentException, IllegalAccessException {
        try {
            final Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return (Integer) f.get(process);
        } catch (final NoSuchFieldException e) {
            throw new IllegalArgumentException("Strange. Seems like you are on a unix based system, yet "
                    + "there is no private field named 'pid'. This should work on Java 8. You may need to "
                    + "add new code to address the JVM you are running on. Or are you running on Windows?", e);
        }
    }


}
