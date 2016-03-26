package com.aboutsip.performance.core.sipp;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 *
 */
public class DefaultSIPpTest {

    @Test
    public void testUASandUAC() throws Exception {

        final SIPpManager manager = new SIPpManager();
        final SIPp uas = manager.newInstance()
                .withListenPort(5060)
                .withListenAddress("127.0.0.1")
                .withScenario("uas")
                .withFriendlyName("uas")
                .build();

        final SIPp uac = manager.newInstance()
                .withScenario("uac")
                .withInitialRate(0)
                .withRemoteHost("127.0.0.1")
                .withRemotePort(5060)
                .withFriendlyName("uac")
                .build();

        CompletableFuture.allOf(uas.start(), uac.start()).join();

        new Thread(() -> {
            while (true) {
                System.out.println("UAC -> " + uac.getStats().getResponseTimeRepartition1().getResponseTimes().get(0));
                System.out.println("UAS -> " + uas.getStats().getResponseTimeRepartition1().getResponseTimes().get(0));
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                }
            }
        }).start();

        for (int i = 1; i <= 5; ++i) {
            Thread.sleep(1000);
            uac.setRate(i * 10);
        }

        System.err.println("Ok, Done increasing stuff...");
        Thread.sleep(10000);
        System.err.println("how are things going?");
        Thread.sleep(20000);

        CompletableFuture.allOf(uac.pause().join().stop(true), uas.stop(true)).join();
    }


    @Test
    public void testStartSIPp() throws Exception {
        final SIPpManager manager = new SIPpManager();
        final SIPp sipp = manager.newInstance()
                .withScenario("uac")
                .withInitialRate(2)
                .withRemoteHost("127.0.0.1")
                .withRemotePort(5060)
                .withFriendlyName("testing")
                .build();
        CompletableFuture<SIPp> future = sipp.start();
        System.err.println(Thread.currentThread().getName() + " Started a new SIPp");
        future.get();

        new Thread(() -> {
            while (true) {
                // System.out.println("Target rate: " + sipp.getTargetRate() + " Current Rate: " + sipp.getCurrentRate());
                System.out.println(sipp.getStats().getResponseTimeRepartition1().getResponseTimes().get(0));
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                }
            }
        }).start();

        for (int i = 1; i <= 5; ++i) {
            Thread.sleep(1000);
            sipp.setRate(i * 10);
        }

        System.err.println("Ok, Done increasing stuff...");
        Thread.sleep(10000);
        System.err.println("how are things going?");
        Thread.sleep(20000);

        System.err.println("pausing");
        sipp.pause();
        Thread.sleep(10000);

        System.err.println("Stopping");
        sipp.stop(true).get(1000, TimeUnit.MILLISECONDS);
        System.err.println("Stopped");
        // sipp.cleanUp();
        System.err.println("Did i remove all files???");

    }

    private void loopAndPrint(final SIPp sipp, final Consumer<SIPp> f, final String msg) throws Exception {
        for (int i = 0; i < 3; ++i) {
            System.out.println(msg);
            f.accept(sipp);
            Thread.sleep(100);
        }

    }
}
