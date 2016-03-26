package com.aboutsip.performance.core.sipp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 */
public interface StatsObject {

    /**
     * SIPp has its own kind of duration like so HH:mm:ss[:SSS] but that can't be parsed e.g. as a
     * {@link Duration} so we are building up one manually.
     *
     * @param sippDuration
     * @return
     * @throws NumberFormatException
     */
    static Duration toDuration(final String sippDuration) throws NumberFormatException {
        final String[] parts = sippDuration.split(":");
        final int hour = Integer.parseInt(parts[0]);
        final int min = Integer.parseInt(parts[1]);
        final int seconds = Integer.parseInt(parts[2]);
        final int millis = parts.length == 4 ? Integer.parseInt(parts[3]) : 0;

        return Duration.ofHours(hour)
                .plusMinutes(min)
                .plusSeconds(seconds)
                .plusMillis(millis);
    }

    /**
     * The time at which these stats were recorded.
     *
     * @return
     */
    LocalDateTime getTime();


    LocalDateTime getStartTime();

    LocalDateTime getLastResetTime();

    LocalDateTime getCurrentTime();

    Duration getElapsedTime();

    Duration getElapsedTimeCumulative();

    /**
     * Get the target rate, which may or may not be the same as the current rate.
     *
     * @return
     */
    int getTargetRate();

    /**
     * The current rate of SIPp.
     *
     * You may try to e.g. reach 100 CPS but either
     * the SUT (server under test) can't keep up with that volume, or SIPp itself
     * cannot keep up and if so, the actual rate may differ from the target rate.
     *
     * Note: this is the SIPp "periodic current rate", i.e., this is the rate as compared
     * to the last one reported.
     *
     * @return
     */
    double getCallRate();

    double getCallRateCumulative();

    int getIncomingCall();
    int getIncomingCallCumulative();
    int getOutgoingCall();
    int getOutgoingCallCumulative();
    int getTotalCallCreated();
    int getCurrentCall();
    int getSuccessfulCall();
    int getSuccessfulCallCumulative();
    int getFailedCall();
    int getFailedCallCumulative();
    int getFailedCannotSendMessage();
    int getFailedCannotSendMessageCumulative();
    int getFailedMaxUDPRetrans();
    int getFailedMaxUDPRetransCumulative();
    int getFailedTcpConnect();
    int getFailedTcpConnectCumulative();
    int getFailedTcpClosed();
    int getFailedTcpClosedCumulative();
    int getFailedUnexpectedMessage();
    int getFailedUnexpectedMessageCumulative();
    int getFailedCallRejected();
    int getFailedCallRejectedCumulative();
    int getFailedCmdNotSent();
    int getFailedCmdNotSentCumulative();
    int getFailedRegexpDoesntMatch();
    int getFailedRegexpDoesntMatchCumulative();
    int getFailedRegexpShouldntMatch();
    int getFailedRegexpShouldntMatchCumulative();
    int getFailedRegexpHdrNotFound();
    int getFailedRegexpHdrNotFoundCumulative();
    int getFailedOutboundCongestion();
    int getFailedOutboundCongestionCumulative();
    int getFailedTimeoutOnRecv();
    int getFailedTimeoutOnRecvCumulative();
    int getFailedTimeoutOnSend();
    int getFailedTimeoutOnSendCumulative();
    int getOutOfCallMsgs();
    int getOutOfCallMsgsCumulative();
    int getDeadCallMsgs();
    int getDeadCallMsgsCumulative();
    int getRetransmissions();
    int getRetransmissionsCumulative();
    int getAutoAnswered();
    int getAutoAnsweredCumulative();
    int getWarnings();
    int getWarningsCumulative();
    int getFatalErrors();
    int getFatalErrorsCumulative();
    int getWatchdogMajor();
    int getWatchdogMajorCumulative();
    int getWatchdogMinor();
    int getWatchdogMinorCumulative();
    Duration getResponseTime1();
    Duration getResponseTime1Cumulative();
    Duration getResponseTime1StDev();
    Duration getResponseTime1StDevCumulative();
    Duration getCallLength();
    Duration getCallLengthCumulative();
    String getCallLengthStDev();
    String getCallLengthStDevCumulative();
    ResponseTimeRepartition getResponseTimeRepartition1();
    String getCallLengthRepartition();


}
