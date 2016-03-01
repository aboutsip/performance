package com.aboutsip.performance.core.sipp;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public class DefaultSIPpTest {

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
                System.out.println("Target rate: " + sipp.getTargetRate() + " Current Rate: " + sipp.getCurrentRate());
                try {
                    Thread.sleep(300);
                } catch (final InterruptedException e) {
                }
            }
        }).start();

        for (int i = 1; i <= 5; ++i) {
            Thread.sleep(1000);
            sipp.setRate(i * 10);
        }

        System.err.println("Stopping");
        sipp.stop(true).get(1000, TimeUnit.MILLISECONDS);
        System.err.println("Stopped");
        sipp.cleanUp();
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
