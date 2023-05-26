/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.coordinator;

final class TimeoutValueAdjuster {
    private static final String TIMEOUT_FACTOR_PROPERTY = "lra.test.timeout.factor";
    private static final String timeoutFactorString = System.getProperty(TIMEOUT_FACTOR_PROPERTY, "1.0");
    private static final double timeoutFactor = Double.parseDouble(timeoutFactorString);

    private TimeoutValueAdjuster() {
        // not for initialization
    }

    static long adjustTimeout(long originalTimeout)  {
        if (timeoutFactor <= 0) {
            return originalTimeout;
        }
        return (long) Math.ceil(originalTimeout * timeoutFactor);
    }
}