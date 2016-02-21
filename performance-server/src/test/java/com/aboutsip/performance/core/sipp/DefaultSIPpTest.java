package com.aboutsip.performance.core.sipp;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DefaultSIPpTest {

    @Test
    public void testStartSIPp() throws Exception {
        final SIPpManager manager = new SIPpManager();
        final SIPp sipp = manager.newInstance()
                .withScenario("uac")
                .withRemoteHost("127.0.0.1")
                .withRemotePort(5060)
                .withFriendlyName("testing")
                .build();
        CompletableFuture<SIPp> future = sipp.start();
        System.err.println(Thread.currentThread().getName() + " Started a new SIPp");
        future.get();
        Thread.sleep(10000);
        System.err.println("Stopping");
        sipp.stop(true).get(1000, TimeUnit.MILLISECONDS);
        System.err.println("Stopped");

    }
}
