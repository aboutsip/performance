package com.aboutsip.performance.core.sipp;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.DateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StatsLabelsTest extends SIPpTestBase{

    @Test
    public void testReadStatsLabels() throws Exception {
        final String line = getFirstLineOfResource("uac_20157_.csv");
        final StatsLabels labels = StatsLabels.create(SIPp.Version.THREE_DOT_THREE, line);
        assertThat(labels.findIndex(StatsLabels.START_TIME), is(0));
        assertThat(labels.findIndex(StatsLabels.CURRENT_TIME), is(2));
        assertThat(labels.findIndex(StatsLabels.TARGET_RATE), is(5));
        assertThat(labels.findIndex(StatsLabels.CALL_RATE_P), is(6));
        assertThat(labels.findIndex(StatsLabels.CALL_RATE_P), is(7));

        System.out.println(labels);
    }

    @Test
    public void testReadStats() throws Exception {
        final BufferedReader reader = loadResourceFile("uac_20157_.csv");
        final StatsLabels labels = StatsLabels.create(SIPp.Version.THREE_DOT_THREE, reader.readLine());

        final StatsObject stats1 = labels.createNewStats(getNthLine(reader, 7));
        assertThat(stats1.getTargetRate(), is(2));
        assertThat(stats1.getCurrentCall(), is(0));

        assertThat(stats1.getCallRate(), is(1.998d));
        assertThat(stats1.getCallRateCumulative(), is(1.99568d));

        assertThat(stats1.getTotalCallCreated(), is(12));
        assertThat(stats1.getSuccessfulCall(), is(2));
        assertThat(stats1.getSuccessfulCallCumulative(), is(12));

        assertThat(stats1.getIncomingCall(), is(0));
        assertThat(stats1.getIncomingCallCumulative(), is(0));

        assertThat(stats1.getOutgoingCall(), is(2));
        assertThat(stats1.getOutgoingCallCumulative(), is(12));

        assertThat(stats1.getTime().toString(), is("2016-02-26T15:13:45.896"));
    }

    @Test
    public void testShit() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        assertToDuration(18, 14, 43, 202);
    }

    private void assertToDuration(final long hr, final long min, final long s, final long millis) {
        final String toParse = hr + ":" + min + ":" + s + (millis > 0 ? ":" + millis : "");
        final long expectedMinutes = hr * 60 + min;
        final long expectedSeconds = expectedMinutes * 60 + s;

        final int expectedNanos = (int)(millis > 0 ? millis * 1000 * 1000 : 0);

        final Duration duration = StatsObject.toDuration(toParse);
        System.out.println(toParse + " -> " + duration);

        assertThat(duration.toHours(), is(hr));
        assertThat(duration.toMinutes(), is(expectedMinutes));
        assertThat(duration.getSeconds(), is(expectedSeconds));
        assertThat(duration.getNano(), is(expectedNanos));
    }


    @Test
    public void testReadAll() throws Exception {
        final BufferedReader reader = loadResourceFile("uac_20157_.csv");
        final StatsLabels labels = StatsLabels.create(SIPp.Version.THREE_DOT_THREE, reader.readLine());

        reader.lines().forEach(line -> {
            final StatsObject stats1 = labels.createNewStats(line);
            accessAllFields(stats1);
        });

    }

    private void accessAllFields(final StatsObject stats1) {
        stats1.getStartTime();
        stats1.getLastResetTime();
        stats1.getCurrentTime();
        stats1.getElapsedTime();
        stats1.getElapsedTimeCumulative();
        stats1.getTargetRate();
        stats1.getCallRate();
        stats1.getCallRateCumulative();
        stats1.getIncomingCall();
        stats1.getIncomingCallCumulative();
        stats1.getOutgoingCall();
        stats1.getOutgoingCallCumulative();
        stats1.getTotalCallCreated();
        stats1.getCurrentCall();
        stats1.getSuccessfulCall();
        stats1.getSuccessfulCallCumulative();
        stats1.getFailedCall();
        stats1.getFailedCallCumulative();
        stats1.getFailedCannotSendMessage();
        stats1.getFailedCannotSendMessageCumulative();
        stats1.getFailedMaxUDPRetrans();
        stats1.getFailedMaxUDPRetransCumulative();
        stats1.getFailedTcpConnect();
        stats1.getFailedTcpConnectCumulative();
        stats1.getFailedTcpClosed();
        stats1.getFailedTcpClosedCumulative();
        stats1.getFailedUnexpectedMessage();
        stats1.getFailedUnexpectedMessageCumulative();
        stats1.getFailedCallRejected();
        stats1.getFailedCallRejectedCumulative();
        stats1.getFailedCmdNotSent();
        stats1.getFailedCmdNotSentCumulative();
        stats1.getFailedRegexpDoesntMatch();
        stats1.getFailedRegexpDoesntMatchCumulative();
        stats1.getFailedRegexpShouldntMatch();
        stats1.getFailedRegexpShouldntMatchCumulative();
        stats1.getFailedRegexpHdrNotFound();
        stats1.getFailedRegexpHdrNotFoundCumulative();
        stats1.getFailedOutboundCongestion();
        stats1.getFailedOutboundCongestionCumulative();
        stats1.getFailedTimeoutOnRecv();
        stats1.getFailedTimeoutOnRecvCumulative();
        stats1.getFailedTimeoutOnSend();
        stats1.getFailedTimeoutOnSendCumulative();
        stats1.getOutOfCallMsgs();
        stats1.getOutOfCallMsgsCumulative();
        stats1.getDeadCallMsgs();
        stats1.getDeadCallMsgsCumulative();
        stats1.getRetransmissions();
        stats1.getRetransmissionsCumulative();
        stats1.getAutoAnswered();
        stats1.getAutoAnsweredCumulative();
        stats1.getWarnings();
        stats1.getWarningsCumulative();
        stats1.getFatalErrors();
        stats1.getFatalErrorsCumulative();
        stats1.getWatchdogMajor();
        stats1.getWatchdogMajorCumulative();
        stats1.getWatchdogMinor();
        stats1.getWatchdogMinorCumulative();
        stats1.getResponseTime1();
        stats1.getResponseTime1Cumulative();
        stats1.getResponseTime1StDev();
        stats1.getResponseTime1StDevCumulative();
        stats1.getCallLength();
        stats1.getCallLengthCumulative();
        stats1.getCallLengthStDev();
        stats1.getCallLengthStDevCumulative();
        stats1.getResponseTimeRepartition1();
        stats1.getCallLengthRepartition();
    }

    /**
     * Skip ahead and read the Nth line. Note, if you already have read stuff out then this will simply
     * just loop n times so it isn't really nth from the beginning of the file.
     *
     * @param reader
     * @param nth
     * @return
     */
    public String getNthLine(final BufferedReader reader, final int nth) throws IOException {
        for (int i = 0; i < nth - 1; ++i) {
            reader.readLine();
        }

        return reader.readLine();
    }
}
