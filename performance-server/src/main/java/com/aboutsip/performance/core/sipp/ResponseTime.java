package com.aboutsip.performance.core.sipp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;

/**
 * Represents a particular response count for a particular range, i.e.
 * it represents one of the rows as displayed by SIPps response time
 * repartition screen.
 *
 * <pre>
 *       Average Response Time Repartition 1
 *       0 ms   <= n <        10 ms :          0
 *       10 ms  <= n <        20 ms :          0
 *       20 ms  <= n <        30 ms :          0
 *       30 ms  <= n <        40 ms :          0
 *       40 ms  <= n <        50 ms :          0
 *       50 ms  <= n <       100 ms :          0
 *       100 ms <= n <       150 ms :          0
 *       150 ms <= n <       200 ms :          0
 *                 n >=      200 ms :          0
 * </pre>
 *
 */
public interface ResponseTime {

    /**
     * The lower limit (inclusive). E.g. 10ms which means that
     * the counts in this {@link ResponseTime} all came in at or
     * above this time limit.
     *
     * @return
     */
    @JsonProperty
    int lower();

    /**
     * The upper limit (exclusive). E.g., 50 ms, which means that
     * the counts in this {@link ResponseTime} came in under this
     * limit.
     *
     * The one exception is that if the upper limit is -1 then this
     * indicates infinity.
     *
     * @return
     */
    @JsonProperty
    int upper();

    @JsonProperty
    int count();

    /**
     * Represents a count of how many responses came in between the lower (inclusive) and upper (exclusive) bounds.
     * E.g. how many of the responses came in under 10 ms?
     *
     * Note: a special case is the final count in the series where the lowerLimit
     * will be inclusive and the upper limit will be infinity, which will be
     * expressed as -1 (negative one).
     *
     * @param lowerLimit
     * @param upperLimit
     * @param count
     * @return
     */
    static ResponseTime create(final int lowerLimit, final int upperLimit, final int count) {
        if (upperLimit != -1 && lowerLimit > upperLimit) {
            throw new IllegalArgumentException("The lower limit cannot be greater than the upper limit");
        }

        return new DefaultResponseTime(lowerLimit, upperLimit, count);
    }

    class DefaultResponseTime implements ResponseTime {
        private final int lowerLimit;
        private final int upperLimit;
        private final int count;

        private DefaultResponseTime(final int lowerLimit, final int upperLimit, final int value) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.count = value;
        }

        @Override
        public int lower() {
            return lowerLimit;
        }

        @Override
        public int upper() {
            return upperLimit;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public String toString() {
            if (upperLimit == -1) {
                return count + " >= " + lowerLimit;
            }
            return lowerLimit + " <= " + count + " < " + upperLimit;
        }
    }
}
