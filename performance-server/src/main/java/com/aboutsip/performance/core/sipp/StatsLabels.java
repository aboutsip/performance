package com.aboutsip.performance.core.sipp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The statistical file that is produced by SIPp could (is?) be dynamic but the first line
 * will state what the various columns actually are. This interface encapsulates those labels
 * so we know what each column actually means.
 * <p>
 * See what they all mean here: http://sipp.sourceforge.net/doc3.2/reference.html#Statistics.
 * <p>
 * Note, at least for version 3.2, 3.3 and 3.4 the stats has't changed. However, that may change
 * down the line
 */
public interface StatsLabels {

    String START_TIME = "StartTime";
    String LAST_RESET_TIME = "LastResetTime";
    String CURRENT_TIME = "CurrentTime";
    String ELAPSED_TIME_P = "ElapsedTime(P)";
    String ELAPSED_TIME_C = "ElapsedTime(C)";
    String TARGET_RATE = "TargetRate";
    String CALL_RATE_P = "CallRate(P)";
    String CALL_RATE_C = "CallRate(C)";
    String INCOMING_CALL_P = "IncomingCall(P)";
    String INCOMING_CALL_C = "IncomingCall(C)";
    String OUTGOING_CALL_P = "OutgoingCall(P)";
    String OUTGOING_CALL_C = "OutgoingCall(C)";
    String TOTAL_CALL_CREATED = "TotalCallCreated";
    String CURRENT_CALL = "CurrentCall";
    String SUCCESSFUL_CALL_P = "SuccessfulCall(P)";
    String SUCCESSFUL_CALL_C = "SuccessfulCall(C)";
    String FAILED_CALL_P = "FailedCall(P)";
    String FAILED_CALL_C = "FailedCall(C)";
    String FAILED_CANNOT_SEND_MESSAGE_P = "FailedCannotSendMessage(P)";
    String FAILED_CANNOT_SEND_MESSAGE_C = "FailedCannotSendMessage(C)";
    String FAILED_MAX_UDP_Retrans_P = "FailedMaxUDPRetrans(P)";
    String FAILED_MAX_UDP_Retrans_C = "FailedMaxUDPRetrans(C)";
    String FAILED_TCP_CONNECT_P = "FailedTcpConnect(P)";
    String FAILED_TCP_CONNECT_C = "FailedTcpConnect(C)";
    String FAILED_TCP_CLOSED_P = "FailedTcpClosed(P)";
    String FAILED_TCP_CLOSED_C = "FailedTcpClosed(C)";
    String FAILED_UNEXPECTED_MESSAGE_P = "FailedUnexpectedMessage(P)";
    String FAILED_UNEXPECTED_MESSAGE_C = "FailedUnexpectedMessage(C)";
    String FAILED_CALL_REJECTED_P = "FailedCallRejected(P)";
    String FAILED_CALL_REJECTED_C = "FailedCallRejected(C)";
    String FAILED_CMD_NOT_SENT_P = "FailedCmdNotSent(P)";
    String FAILED_CMD_NOT_SENT_C = "FailedCmdNotSent(C)";
    String FAILED_REGEXP_DOESNT_MATCH_P = "FailedRegexpDoesntMatch(P)";
    String FAILED_REGEXP_DOESNT_MATCH_C = "FailedRegexpDoesntMatch(C)";
    String FAILED_REGEXP_SHOULDNT_MATCH_P = "FailedRegexpShouldntMatch(P)";
    String FAILED_REGEXP_SHOULDNT_MATCH_C = "FailedRegexpShouldntMatch(C)";
    String FAILED_REGEXP_HDR_NOT_FOUND_P = "FailedRegexpHdrNotFound(P)";
    String FAILED_REGEXP_HDR_NOT_FOUND_C = "FailedRegexpHdrNotFound(C)";
    String FAILED_OUTBOUND_CONGESTION_P = "FailedOutboundCongestion(P)";
    String FAILED_OUTBOUND_CONGESTION_C = "FailedOutboundCongestion(C)";
    String FAILED_TIMEOUT_ON_RECV_P = "FailedTimeoutOnRecv(P)";
    String FAILED_TIMEOUT_ON_RECV_C = "FailedTimeoutOnRecv(C)";
    String FAILED_TIMEOUT_ON_SEND_P = "FailedTimeoutOnSend(P)";
    String FAILED_TIMEOUT_ON_SEND_C = "FailedTimeoutOnSend(C)";
    String OUT_OF_CALL_MSGS_P = "OutOfCallMsgs(P)";
    String OUT_OF_CALL_MSGS_C = "OutOfCallMsgs(C)";
    String DEAD_CALL_MSGS_P = "DeadCallMsgs(P)";
    String DEAD_CALL_MSGS_C = "DeadCallMsgs(C)";
    String RETRANSMISSIONS_P = "Retransmissions(P)";
    String RETRANSMISSIONS_C = "Retransmissions(C)";
    String AUTO_ANSWERED_P = "AutoAnswered(P)";
    String AUTO_ANSWERED_C = "AutoAnswered(C)";
    String WARNINGS_P = "Warnings(P)";
    String WARNINGS_C = "Warnings(C)";
    String FATAL_ERRORS_P = "FatalErrors(P)";
    String FATAL_ERRORS_C = "FatalErrors(C)";
    String WATCHDOG_MAJOR_P = "WatchdogMajor(P)";
    String WATCHDOG_MAJOR_C = "WatchdogMajor(C)";
    String WATCHDOG_MINOR_P = "WatchdogMinor(P)";
    String WATCHDOG_MINOR_C = "WatchdogMinor(C)";
    String RESPONSE_TIME1_P = "ResponseTime1(P)";
    String RESPONSE_TIME1_C = "ResponseTime1(C)";
    String RESPONSE_TIME1_ST_DEV_P = "ResponseTime1StDev(P)";
    String RESPONSE_TIME1_ST_DEV_C = "ResponseTime1StDev(C)";
    String CALL_LENGTH_P = "CallLength(P)";
    String CALL_LENGTH_C = "CallLength(C)";
    String CALL_LENGTH_ST_DEV_P = "CallLengthStDev(P)";
    String CALL_LENGTH_ST_DEV_C = "CallLengthStDev(C)";
    String RESPONSE_TIME_REPARTITION_1 = "ResponseTimeRepartition1";
    String CALL_LENGTH_REPARTITION = "CallLengthRepartition";


    /**
     * Find the index of a particular label. Use the labels as defined in this
     * interface, such as {@link #TARGET_RATE}.
     *
     * @param label
     * @return
     */
    int findIndex(String label);

    /**
     * Get the label at a specific index.
     *
     * @param index
     * @return the label at that index
     * @throws IllegalArgumentException in case the provided index is out of bounds
     */
    String getLabel(int index) throws IllegalArgumentException;

    /**
     * Create a new {@link StatsObject} where it is assumed that all the values will match up with
     * the labels represented by this object.
     *
     * @param values
     * @return
     * @throws IllegalArgumentException in case the passed in values do not match the labels. E.g.,
     *                                  the number of values are more or less than the number.
     */
    StatsObject createNewStats(List<String> values) throws IllegalArgumentException;

    /**
     * Create a new {@link StatsObject} based on a raw string.
     *
     * @param values
     * @return
     * @throws IllegalArgumentException
     */
    StatsObject createNewStats(String values) throws IllegalArgumentException;

    static StatsLabels create(final SIPp.Version version, final String labels) {
        if (version.ordinal() >= SIPp.Version.THREE_DOT_ZERO.ordinal()
                && version.ordinal() <= SIPp.Version.THREE_DOT_FOUR.ordinal()) {
            final List<String> parts = Stream.of(labels.split(";")).map(String::trim).collect(Collectors.toList());
            return new SIPpVersion3StatsLabel(version, parts);
        }

        // If you run into this, try and see if it works. Perhaps it does...
        throw new IllegalArgumentException("Currently cannot process data from the supplied version");
    }

    /**
     * So far, the 3.x series of SIPp seems to have all the same stats labels...
     */
    class SIPpVersion3StatsLabel implements StatsLabels {

        private final List<String> labels;

        private final SIPp.Version version;

        /**
         * All SIPp version 3 clients are using this date format.
         */
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

        private SIPpVersion3StatsLabel(final SIPp.Version version, final List<String> labels) {
            this.version = version;
            this.labels = labels;
        }

        /**
         * @param label
         * @return
         */
        @Override
        public int findIndex(final String label) {
            for (int i = 0; i < labels.size(); ++i) {
                if (label.equals(labels.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public String getLabel(final int index) throws IllegalArgumentException {
            if (index >= labels.size()) {
                throw new IllegalArgumentException("Index is outside of the range of available labels");
            }
            return labels.get(index);
        }

        @Override
        public String toString() {
            return String.join(", ", labels);
        }

        @Override
        public StatsObject createNewStats(final List<String> values) throws IllegalArgumentException {
            if (labels.size() != values.size()) {
                throw new IllegalArgumentException("The number of values does not match up to the number of labels");
            }

            return new SIPpVersion3StatsObject(this, formatter, values);
        }

        @Override
        public StatsObject createNewStats(String values) throws IllegalArgumentException {
            final List<String> parts = Stream.of(values.split(";")).map(String::trim).collect(Collectors.toList());
            return new SIPpVersion3StatsObject(this, formatter, parts);
        }
    }

    class SIPpVersion3StatsObject implements StatsObject {

        private final SIPpVersion3StatsLabel labels;
        private final List<String> values;
        private final DateTimeFormatter formatter;

        private SIPpVersion3StatsObject(final SIPpVersion3StatsLabel labels, final DateTimeFormatter formatter, final List<String> values) {
            this.labels = labels;
            this.formatter = formatter;
            this.values = values;
        }

        @Override
        public LocalDateTime getTime() {
            return getDate(StatsLabels.CURRENT_TIME);
        }

        @Override
        public LocalDateTime getStartTime() {
            return getDate(StatsLabels.START_TIME);
        }

        @Override
        public LocalDateTime getLastResetTime() {
            return getDate(StatsLabels.LAST_RESET_TIME);
        }

        @Override
        public LocalDateTime getCurrentTime() {
            return getDate(StatsLabels.CURRENT_TIME);
        }

        @Override
        public Duration getElapsedTime() {
            return getDuration(StatsLabels.ELAPSED_TIME_P);
        }

        @Override
        public Duration getElapsedTimeCumulative() {
            return getDuration(StatsLabels.ELAPSED_TIME_C);
        }

        @Override
        public int getTargetRate() {
            return getInteger(StatsLabels.TARGET_RATE);
        }

        @Override
        public double getCallRate() {
            return getDouble(StatsLabels.CALL_RATE_P);
        }

        @Override
        public double getCallRateCumulative() {
            return getDouble(StatsLabels.CALL_RATE_C);
        }

        @Override
        public int getIncomingCall() {
            return getInteger(StatsLabels.INCOMING_CALL_P);
        }

        @Override
        public int getIncomingCallCumulative() {
            return getInteger(StatsLabels.INCOMING_CALL_C);
        }

        @Override
        public int getOutgoingCall() {
            return getInteger(StatsLabels.OUTGOING_CALL_P);
        }

        @Override
        public int getOutgoingCallCumulative() {
            return getInteger(StatsLabels.OUTGOING_CALL_C);
        }

        @Override
        public int getTotalCallCreated() {
            return getInteger(StatsLabels.TOTAL_CALL_CREATED);
        }

        @Override
        public int getCurrentCall() {
            return getInteger(StatsLabels.CURRENT_CALL);
        }

        @Override
        public int getSuccessfulCall() {
            return getInteger(StatsLabels.SUCCESSFUL_CALL_P);
        }

        @Override
        public int getSuccessfulCallCumulative() {
            return getInteger(StatsLabels.SUCCESSFUL_CALL_C);
        }

        @Override
        public int getFailedCall() {
            return getInteger(StatsLabels.FAILED_CALL_P);
        }

        @Override
        public int getFailedCallCumulative() {
            return getInteger(StatsLabels.FAILED_CALL_C);
        }

        @Override
        public int getFailedCannotSendMessage() {
            return getInteger(StatsLabels.FAILED_CANNOT_SEND_MESSAGE_P);
        }

        @Override
        public int getFailedCannotSendMessageCumulative() {
            return getInteger(StatsLabels.FAILED_CANNOT_SEND_MESSAGE_C);
        }

        @Override
        public int getFailedMaxUDPRetrans() {
            return getInteger(StatsLabels.FAILED_MAX_UDP_Retrans_P);
        }

        @Override
        public int getFailedMaxUDPRetransCumulative() {
            return getInteger(StatsLabels.FAILED_MAX_UDP_Retrans_C);
        }

        @Override
        public int getFailedTcpConnect() {
            return getInteger(StatsLabels.FAILED_TCP_CONNECT_P);
        }

        @Override
        public int getFailedTcpConnectCumulative() {
            return getInteger(StatsLabels.FAILED_TCP_CONNECT_C);
        }

        @Override
        public int getFailedTcpClosed() {
            return getInteger(StatsLabels.FAILED_TCP_CLOSED_P);
        }

        @Override
        public int getFailedTcpClosedCumulative() {
            return getInteger(StatsLabels.FAILED_TCP_CLOSED_C);
        }

        @Override
        public int getFailedUnexpectedMessage() {
            return getInteger(StatsLabels.FAILED_UNEXPECTED_MESSAGE_P);
        }

        @Override
        public int getFailedUnexpectedMessageCumulative() {
            return getInteger(StatsLabels.FAILED_UNEXPECTED_MESSAGE_C);
        }

        @Override
        public int getFailedCallRejected() {
            return getInteger(StatsLabels.FAILED_CALL_REJECTED_P);
        }

        @Override
        public int getFailedCallRejectedCumulative() {
            return getInteger(StatsLabels.FAILED_CALL_REJECTED_C);
        }

        @Override
        public int getFailedCmdNotSent() {
            return getInteger(StatsLabels.FAILED_CMD_NOT_SENT_P);
        }

        @Override
        public int getFailedCmdNotSentCumulative() {
            return getInteger(StatsLabels.FAILED_CMD_NOT_SENT_C);
        }

        @Override
        public int getFailedRegexpDoesntMatch() {
            return getInteger(StatsLabels.FAILED_REGEXP_DOESNT_MATCH_P);
        }

        @Override
        public int getFailedRegexpDoesntMatchCumulative() {
            return getInteger(StatsLabels.FAILED_REGEXP_DOESNT_MATCH_C);
        }

        @Override
        public int getFailedRegexpShouldntMatch() {
            return getInteger(StatsLabels.FAILED_REGEXP_SHOULDNT_MATCH_P);
        }

        @Override
        public int getFailedRegexpShouldntMatchCumulative() {
            return getInteger(StatsLabels.FAILED_REGEXP_SHOULDNT_MATCH_C);
        }

        @Override
        public int getFailedRegexpHdrNotFound() {
            return getInteger(StatsLabels.FAILED_REGEXP_HDR_NOT_FOUND_P);
        }

        @Override
        public int getFailedRegexpHdrNotFoundCumulative() {
            return getInteger(StatsLabels.FAILED_REGEXP_HDR_NOT_FOUND_C);
        }

        @Override
        public int getFailedOutboundCongestion() {
            return getInteger(StatsLabels.FAILED_OUTBOUND_CONGESTION_P);
        }

        @Override
        public int getFailedOutboundCongestionCumulative() {
            return getInteger(StatsLabels.FAILED_OUTBOUND_CONGESTION_C);
        }

        @Override
        public int getFailedTimeoutOnRecv() {
            return getInteger(StatsLabels.FAILED_TIMEOUT_ON_RECV_P);
        }

        @Override
        public int getFailedTimeoutOnRecvCumulative() {
            return getInteger(StatsLabels.FAILED_TIMEOUT_ON_RECV_C);
        }

        @Override
        public int getFailedTimeoutOnSend() {
            return getInteger(StatsLabels.FAILED_TIMEOUT_ON_SEND_P);
        }

        @Override
        public int getFailedTimeoutOnSendCumulative() {
            return getInteger(StatsLabels.FAILED_TIMEOUT_ON_SEND_C);
        }

        @Override
        public int getOutOfCallMsgs() {
            return getInteger(StatsLabels.OUT_OF_CALL_MSGS_P);
        }

        @Override
        public int getOutOfCallMsgsCumulative() {
            return getInteger(StatsLabels.OUT_OF_CALL_MSGS_C);
        }

        @Override
        public int getDeadCallMsgs() {
            return getInteger(StatsLabels.DEAD_CALL_MSGS_P);
        }

        @Override
        public int getDeadCallMsgsCumulative() {
            return getInteger(StatsLabels.DEAD_CALL_MSGS_C);
        }

        @Override
        public int getRetransmissions() {
            return getInteger(StatsLabels.RETRANSMISSIONS_P);
        }

        @Override
        public int getRetransmissionsCumulative() {
            return getInteger(StatsLabels.RETRANSMISSIONS_C);
        }

        @Override
        public int getAutoAnswered() {
            return getInteger(StatsLabels.AUTO_ANSWERED_P);
        }

        @Override
        public int getAutoAnsweredCumulative() {
            return getInteger(StatsLabels.AUTO_ANSWERED_C);
        }

        @Override
        public int getWarnings() {
            return getInteger(StatsLabels.WARNINGS_P);
        }
        @Override
        public int getWarningsCumulative() {
            return getInteger(StatsLabels.WARNINGS_C);
        }

        @Override
        public int getFatalErrors() {
            return getInteger(StatsLabels.FATAL_ERRORS_P);
        }

        @Override
        public int getFatalErrorsCumulative() {
            return getInteger(StatsLabels.FATAL_ERRORS_C);
        }

        @Override
        public int getWatchdogMajor() {
            return getInteger(StatsLabels.WATCHDOG_MAJOR_P);
        }

        @Override
        public int getWatchdogMajorCumulative() {
            return getInteger(StatsLabels.WATCHDOG_MAJOR_C);
        }

        @Override
        public int getWatchdogMinor() {
            return getInteger(StatsLabels.WATCHDOG_MINOR_P);
        }

        @Override
        public int getWatchdogMinorCumulative() {
            return getInteger(StatsLabels.WATCHDOG_MINOR_C);
        }

        @Override
        public Duration getResponseTime1() {
            return getDuration(StatsLabels.RESPONSE_TIME1_P);
        }

        @Override
        public Duration getResponseTime1Cumulative() {
            return getDuration(StatsLabels.RESPONSE_TIME1_C);
        }

        @Override
        public Duration getResponseTime1StDev() {
            return getDuration(StatsLabels.RESPONSE_TIME1_ST_DEV_P);
        }

        @Override
        public Duration getResponseTime1StDevCumulative() {
            return getDuration(StatsLabels.RESPONSE_TIME1_ST_DEV_C);
        }

        @Override
        public Duration getCallLength() {
            return getDuration(StatsLabels.CALL_LENGTH_P);
        }

        @Override
        public Duration getCallLengthCumulative() {
            return getDuration(StatsLabels.CALL_LENGTH_C);
        }

        @Override
        public String getCallLengthStDev() {
            return getString(StatsLabels.CALL_LENGTH_ST_DEV_P);
        }

        @Override
        public String getCallLengthStDevCumulative() {
            return getString(StatsLabels.CALL_LENGTH_ST_DEV_C);
        }

        @Override
        public ResponseTimeRepartition getResponseTimeRepartition1() {
            final int index = labels.findIndex(StatsLabels.RESPONSE_TIME_REPARTITION_1);
            boolean done = false;
            int i = index;

            int lowerLimit = 0;

            final List<ResponseTime> responseTimes = new ArrayList<>(9);

            while (!done && ++i < values.size()) {
                final String label = labels.getLabel(i);
                if (label.startsWith("<") || (label.startsWith(">="))) {
                    final int count = Integer.parseInt(values.get(i));
                    int upperLimit = parseLabel(label);

                    // The SIPp special case where we have the last value
                    // and as such the upper limit is now infinity
                    if (upperLimit == lowerLimit) {
                        upperLimit = -1;
                    }
                    responseTimes.add(ResponseTime.create(lowerLimit, upperLimit, count));
                    lowerLimit = upperLimit;
                } else {
                    done = true;
                }
            }

            return ResponseTimeRepartition.create(responseTimes);
        }

        private int parseLabel(final String label) {
            return label.chars().map(c -> c - 48).filter(c -> c >= 0 && c < 10).reduce(0, (left, right) -> left * 10 + right);
        }


        @Override
        public String getCallLengthRepartition() {
            return getString(StatsLabels.CALL_LENGTH_REPARTITION);
        }

        private Duration getDuration(final String label) {
            final String value = values.get(labels.findIndex(label));
            return StatsObject.toDuration(value);
        }

        private LocalDateTime getDate(final String label) {
            final String[] parts = values.get(labels.findIndex(StatsLabels.CURRENT_TIME)).split("\t");
            return LocalDateTime.parse(String.join(" ", parts[0], parts[1]), formatter);
        }

        private String getString(final String label) {
            return values.get(labels.findIndex(label));
        }

        private int getInteger(final String label) {
            final String value = values.get(labels.findIndex(label));
            try {
                return Integer.parseInt(value);
            } catch (final NumberFormatException e) {
                System.err.println(label + " " + value + " is NOT an integerk ");
                return -1;
            }
        }

        private double getDouble(final String label) {
            final String value = values.get(labels.findIndex(label));
            try {
            return Double.parseDouble(value);
            } catch (final NumberFormatException e) {
                System.err.println(label + " " + value + " is NOT a double ");
                return -1;
            }
        }

    }
}
