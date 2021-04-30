/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.perf;

import java.io.*;
import java.util.*;

public class RegressionChecker {
    public final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";
    public static final String RESET_NETRICS_PROP = "io.narayana.perf.resetmetrics";
    public static final String FAIL_ON_PERF_REGRESSION_PROP = "io.narayana.perf.failonregression";

    private final static String BASE_DIR = System.getProperty(BASE_DIRECTORY_PROPERTY);

    public final static String PERF_ARGS_FILENAME = "PerformanceProfileStore.args";
    public final static String PERF_DATA_FILENAME = "PerformanceProfileStore.last";
    public final static String PERF_VAR_FILENAME = "PerformanceProfileStore.var";

    public final static String PROPFILE_COMMENT =
            "Performance profile. Format is testName=value where value is the metric (throughput or duration)";

    public final static Double DEFAULT_VARIANCE = 1.1; // percentage _variance that can be tolerated
    private final static boolean regressionChecksEnabled = readBooleanProperty(FAIL_ON_PERF_REGRESSION_PROP);

    private String testHistoryFileName;
    private Properties testArgs;
    private Properties testHistory;
    private Properties testVariances;

    private boolean resetMetrics = readBooleanProperty(RESET_NETRICS_PROP);
    private boolean failOnRegression = regressionChecksEnabled;

    public static boolean isRegressionCheckEnabled() {
        return regressionChecksEnabled;
    }

    public RegressionChecker() throws IOException {
        this(BASE_DIR + File.separator + PERF_ARGS_FILENAME,
                BASE_DIR + File.separator + PERF_DATA_FILENAME,
                BASE_DIR + File.separator + PERF_VAR_FILENAME);
    }

    public RegressionChecker(String perfArgsFileName, String perfHistoryFileName, String perfVarFileName) throws IOException {
        testHistoryFileName = perfHistoryFileName;
        testArgs = loadProperties(perfArgsFileName);
        testHistory = loadProperties(perfHistoryFileName);
        testVariances = loadProperties(perfVarFileName);
    }

    public static Boolean readBooleanProperty(String propName) {
        return System.getProperty(propName) == null ?  false : Boolean.getBoolean(propName);
    }

    /**
     * If isFailOnRegression is false then new metrics will not
     * be persisted to the metrics store
     * @return  whether or not to mark regressions as failures
     */
    public boolean isFailOnRegression() {
        return failOnRegression;
    }
    public boolean isResetMetrics() {
        return resetMetrics;
    }
    public void setFailOnRegression(boolean failOnRegression) {
        this.failOnRegression = failOnRegression;
    }
    public void setResetMetrics(boolean resetMetrics) {
        this.resetMetrics = resetMetrics;
    }

    private Properties loadProperties(String fileName) throws IOException {
        File file = new File(fileName);
        Properties p = new Properties();

        if (!file.exists())
            file.createNewFile();

        InputStream is = new FileInputStream(file);

        p.load(is);
        is.close();

        return p;
    }

    /**
     * Convert a String to another type
     *
     * @param metricName the name of the test to use if there was a data format error
     * @param args arguments returned from a prior call to {@link RegressionChecker#getTestArgs(String)}
     * @param index index into the args array to the value to be converted
     * @param defaultValue default value if the index is out of range
     * @param argClass class type to convert the value to (which must have a constructor that takes a String value)
     * @param <T> the type which args[index] should be converted to
     * @return the converted value
     */
    public <T> T getArg(String metricName, String[] args, int index, T defaultValue, Class<T> argClass)  {
        if (index >= 0 && index < args.length) {
            try {
                return argClass.getConstructor(String.class).newInstance(args[index]);
            } catch (Exception e) {
                throw new NullPointerException(metricName + ": found invalid test arguments in the PerformanceProfileStore: " + e.getMessage());
            }
        }

        return defaultValue;
    }

    /**
     * Lookup any configured test arguments
     *
     * @param metricName the name of the test
     * @return any test arguments or an empty array if there are none
     */
    public String[] getTestArgs(String metricName) {
        String argsString = System.getProperty(metricName + ".args");

        if (argsString == null)
            argsString = testArgs.getProperty(metricName, "");

        String[] args = argsString.split(",");
        List<String> list = new ArrayList<>(Arrays.asList(args));

        list.removeAll(Collections.singleton(""));

        return list.toArray(new String[list.size()]);
    }

    /**
     * get metrics matching a particular pattern
     * @param pattern the regex used as the pattern
     * @return a map of matching metric names to the current value of the metric
     */
    public Map<String, Double> getMatchingMetrics(String pattern) {
        Map<String, Double> matches = new HashMap<>();
        Enumeration e = testHistory.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            if (key.matches(pattern))
                matches.put(key, Double.valueOf(testHistory.getProperty(key))); // data must contain numbers
        }

        return matches;
    }

    boolean isWithinTolerance(StringBuilder info, String metricName, double metricValue, double canonicalValue, double variance,
                              boolean largerIsBetter) {
        double headRoom = Math.abs(canonicalValue * (variance - 1));
        boolean within;
        double difference = (metricValue - canonicalValue) / canonicalValue * 100;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        String s = String.format("%s %s: %f%% performance %s (%f versus %f) (variance=%f headroom=%f)",
                metricName, within ? "Passed" : "Failed", difference,
                within ? "difference" : "regression", metricValue, canonicalValue, variance, headRoom);

        if (info != null)
            info.append(s);
        else
            System.out.printf("%s%n", s);

        return within;
    }

    boolean isBetter(double metricValue, double canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }

    public double getVariance(String metricName) {
        if (!testVariances.containsKey(metricName))
            return DEFAULT_VARIANCE;

        return Double.parseDouble(testVariances.getProperty(metricName));
    }

    double getMetric(String name, double defaultValue) {
        return Double.parseDouble(testHistory.getProperty(name, Double.toString(defaultValue)));
    }

    boolean updateMetric(StringBuilder info, String metricName, double metricValue, boolean largerIsBetter) {
        return updateMetric(info, getVariance(metricName), metricName, metricValue, largerIsBetter);
    }
    boolean updateMetric(StringBuilder info, double variance, String metricName, double metricValue, boolean largerIsBetter) {
        double canonicalValue =  resetMetrics ? metricValue : getMetric(metricName, metricValue);

        boolean better = isBetter(metricValue, canonicalValue, largerIsBetter);

        if (!testHistory.containsKey(metricName) || better || resetMetrics) {
            testHistory.put(metricName, Double.toString(metricValue));

            if (failOnRegression && testHistoryFileName != null) {
                try {
                    testHistory.store(new FileOutputStream(testHistoryFileName), PROPFILE_COMMENT);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot store performance data", e);
                }
            }
        }

        return isWithinTolerance(info, metricName, metricValue, canonicalValue, variance, largerIsBetter);
    }
}
