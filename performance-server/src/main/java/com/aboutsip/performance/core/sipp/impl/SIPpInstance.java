package com.aboutsip.performance.core.sipp.impl;

import com.aboutsip.performance.core.sipp.SIPp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

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
    private final BufferedReader input;
    private final BufferedWriter output;
    private final BufferedReader error;

    // Pointers to the various files that sipp opens.
    // We need these to pull stats out of.
    private final BufferedReader countsFile;
    private final BufferedReader statsFile;

    /**
     * Used as a lock. Nothing fancy needed...
     */
    private final Object lock = new Object();

    private SIPpInstance(final ScheduledExecutorService executorService,
                         final int pid,
                         final Process process,
                         final BufferedReader input,
                         final BufferedWriter output,
                         final BufferedReader error,
                         final BufferedReader countsFile,
                         final BufferedReader statsFile) {
        this.executorService = executorService;
        this.pid = pid;
        this.process = process;
        this.input = input;
        this.output = output;
        this.error = error;
        this.countsFile = countsFile;
        this.statsFile = statsFile;
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

                    final BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    Thread.sleep(10);
                    if (!process.isAlive()) {
                        System.err.println("ah! Process is dead!");
                        error.lines().forEach(l -> System.err.println(l));
                    }

                    final String countsFileName = String.format("%s_%d_counts.csv", name, pid);
                    final String statsFileName = String.format("%s_%d_.csv", name, pid);

                    // Guess I should schedule a task instead of waiting but easy
                    // and is enough for now...
                    final BufferedReader countsFile = openStatsFile(countsFileName);
                    final BufferedReader statsFile = openStatsFile(statsFileName);

                    final SIPpInstance instance = new SIPpInstance(executorService, pid, process,
                            input, output, error, countsFile, statsFile);

                    future.complete(instance);

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
        System.err.println("Ok, killing the process");
        if (process.isAlive()) {
            System.err.println("Process is still alive. Killing");
            process.destroyForcibly();
        } else {
            System.err.println("Process is already dead");
        }
        return this;
    }

    private SIPpInstance cleanUp() {
        System.err.println("Ok, cleaning up");
        close(countsFile);
        close(statsFile);
        return this;
    }

    private void close(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException e) {
            // ignore
        }
    }

    private static BufferedReader openStatsFile(final String fileName) throws FileNotFoundException {
        final long MAX_WAIT = 1000;
        final File file = new File(fileName);

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
