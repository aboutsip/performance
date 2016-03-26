package com.aboutsip.performance.core.sipp;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public interface ResponseTimeRepartition {

    List<ResponseTime> getResponseTimes();

    static ResponseTimeRepartition create(final List<ResponseTime> responseTimes) {
        if (responseTimes == null || responseTimes.isEmpty()) {
            throw new IllegalArgumentException("You must supply a list of response times");
        }

        return new DefaultResponseTimeRepartition(Collections.unmodifiableList(responseTimes));
    }


    class DefaultResponseTimeRepartition implements ResponseTimeRepartition {
        final List<ResponseTime> responseTimes;

        private DefaultResponseTimeRepartition(final List<ResponseTime> responseTimes) {
            this.responseTimes = responseTimes;
        }

        @Override
        public List<ResponseTime> getResponseTimes() {
            return responseTimes;
        }

        @Override
        public String toString() {
            return "Average Response Time Repartition"
                    + responseTimes.stream().map(Object::toString).reduce("", (s, s2) -> s + "\n" + s2);
        }
    }


}
