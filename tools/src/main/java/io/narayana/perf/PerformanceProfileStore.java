/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package io.narayana.perf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

/**
 * Maintain performance data and check for regressions.
 * Performance data and configuration is stored in a directory named by the system property {@link PerformanceProfileStore#BASE_DIRECTORY_PROPERTY}:
 * - PerformanceProfileStore.last holds the best performance run keyed by the name of the test
 * - PerformanceProfileStore.variance contains the variance for a test (keyed by test name) or, if not present, then the default variance
 *   (eg 1.1 indicates a variance of lest than 10%)
 * - PerformanceProfileStore.args contains any arguments required by a test keyed by test name with value a comma separated string
 *   (the configured values for arguments can be overridden by setting a system property called "testname.args" to the new value)
  * To disable regression checks set the boolean property {@link PerformanceProfileStore#FAIL_ON_PERF_REGRESSION_PROP}
 * To reset performance data for a test set the boolean property {@link PerformanceProfileStore#RESET_NETRICS_PROP}
 */
public class PerformanceProfileStore
{
    public final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";

    private static final boolean DEFAULT_FAIL_ON_REGRESSION = false;

    public final static Float DEFAULT_VARIANCE = 1.1F; // percentage _variance that can be tolerated
    public final static String PERFDATAFILENAME = "PerformanceProfileStore.last";
    public final static String PERFVARIANCEFILENAME = "PerformanceProfileStore.variance";
    public final static String PERFARGSFILENAME = "PerformanceProfileStore.args";
    public final static String PROPFILE_COMMENT =
            "Performance profile. Format is testName=value where value is the metric (throughput or duration)";

    private final static String BASE_DIR = System.getProperty(BASE_DIRECTORY_PROPERTY);
    public static final String FAIL_ON_PERF_REGRESSION_PROP = "io.narayana.perf.failonregression";
    private static boolean failOnRegression = isFailOnRegression();

    public static final String RESET_NETRICS_PROP = "io.narayana.perf.resetmetrics";
    public static final boolean resetMetrics = isResetMetrics();


    private final static PerformanceProfileStore metrics = new PerformanceProfileStore();

    private Properties data;
    private Properties variances;
    private Properties testArgs;
    private File dataFile;
    private float _variance;

    public static boolean isResetMetrics() {
        return System.getProperty(RESET_NETRICS_PROP) == null ?  false :
                Boolean.getBoolean(RESET_NETRICS_PROP);
    }

    public static boolean isFailOnRegression() {
        return System.getProperty(FAIL_ON_PERF_REGRESSION_PROP) == null ?  DEFAULT_FAIL_ON_REGRESSION :
                Boolean.getBoolean(FAIL_ON_PERF_REGRESSION_PROP);
    }

    public static float getVariance() {
        return metrics._variance;
    }

    public static float getVariance(String metricName) {
        if (!metrics.variances.containsKey(metricName))
            return metrics._variance;

        return Float.parseFloat(metrics.variances.getProperty(metricName));
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

    public PerformanceProfileStore() {
        if (!failOnRegression)
            System.out.printf("PerformanceProfileStore: Regression checks are disabled%n");

        if (BASE_DIR == null) {
            System.out.printf(
                    "PerformanceProfileStore: Regression checks are disabled - performance test profile property %s not set%n",
                    BASE_DIRECTORY_PROPERTY);

            data =  new Properties();
            variances =  new Properties();
            testArgs =  new Properties();
            _variance = DEFAULT_VARIANCE;
        } else {

            try {
                String dataFileName = BASE_DIR + File.separator + PERFDATAFILENAME;

                data = loadProperties(dataFileName);
                variances = loadProperties(BASE_DIR + File.separator + PERFVARIANCEFILENAME);
                testArgs = loadProperties(BASE_DIR + File.separator + PERFARGSFILENAME);

                _variance = Float.parseFloat(variances.getProperty("default", DEFAULT_VARIANCE.toString()));
                dataFile = new File(dataFileName);
            } catch (IOException e) {
                throw new RuntimeException("Cannot load performance profile config - please check file paths", e);
            }
        }
    }

    public static Float getMetric(String metricName) {
        if (metrics.data.containsKey(metricName))
            return Float.parseFloat(metrics.data.getProperty(metricName, null));

        return null;
    }

    float getMetric(String name, float defaultValue) {
        return Float.parseFloat(data.getProperty(name, Float.toString(defaultValue)));
    }

    boolean updateMetric(StringBuilder info, String metricName, Float metricValue) {
        return updateMetric(info, _variance, metricName, metricValue, false);
    }

    boolean updateMetric(StringBuilder info, String metricName, Float metricValue, boolean largerIsBetter) {
        return updateMetric(info, getVariance(metricName), metricName, metricValue, largerIsBetter);
    }

    boolean updateMetric(StringBuilder info, float variance, String metricName, Float metricValue, boolean largerIsBetter) {
        Float canonicalValue =  resetMetrics ? metricValue : getMetric(metricName, metricValue);

        boolean better = isBetter(metricValue, canonicalValue, largerIsBetter);

        if (!data.containsKey(metricName) || better || resetMetrics) {
            data.put(metricName, Float.toString(metricValue));

            if ((BASE_DIR != null)) {
                try {
                    data.store(new FileOutputStream(dataFile), PROPFILE_COMMENT);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot store performance data", e);
                }
            }
        }

        return isWithinTolerance(info, metricName, metricValue, canonicalValue, variance, largerIsBetter);
    }

    public static boolean checkPerformance(String metricName, float metricValue) {
        return checkPerformance(metricName, metricValue, false);
    }

    public static boolean checkPerformance(String metricName, float metricValue, boolean largerIsBetter) {
        //return checkPerformance(metricName, getVariance(metricName), metricValue, largerIsBetter);
        return metrics.updateMetric(null, getVariance(metricName), metricName, metricValue, largerIsBetter);
    }

    public static boolean checkPerformance(StringBuilder info, String metricName, float metricValue, boolean largerIsBetter) {
        //return checkPerformance(metricName, getVariance(metricName), metricValue, largerIsBetter);
        return metrics.updateMetric(info, getVariance(metricName), metricName, metricValue, largerIsBetter);
    }


/*    public static boolean checkPerformance(String metricName, float variance, float metricValue, boolean largerIsBetter) {
        return metrics.updateMetric(variance, metricName, metricValue, largerIsBetter);
    }*/

    boolean isWithinTolerance(StringBuilder info, String metricName, Float metricValue, Float canonicalValue, Float variance,
                              boolean largerIsBetter) {
        Float headRoom = Math.abs(canonicalValue * (variance - 1));
        boolean within;
        Float difference = (metricValue - canonicalValue) / canonicalValue * 100;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        boolean ok =  within || !failOnRegression;

        String s = String.format("%s %s: %f%% performance %s (%f versus %f) (variance=%f headroom=%f)",
                metricName, ok ? "Passed" : "Failed", difference,
                within ? "difference" : "regression", metricValue, canonicalValue, variance, headRoom);

        if (info != null)
            info.append(s);
        else
            System.out.printf("%s%n", s);

        return ok;
    }

    boolean isBetter(Float metricValue, Float canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }
    /**
     * Measure the performance of a workload. The returned {@link Measurement} object contains the results of the measurement.
     *
     * @param workload the actual workload being measured
     * @param metricName the name of the test used as a key into performance data {@link PerformanceProfileStore#PERFDATAFILENAME}
     * @param useConfigArgs if true read test arguments from a file ({@link PerformanceProfileStore#getTestArgs(String)}
     * @param warmUpCount Number of iterations of the workload to run before starting the measurement
     * @param numberOfCalls Number of workload iterations (workload is called in batchSize batches until numberOfCalls is reached)
     * @param threadCount Number of threads used to complete the workload
     * @param batchSize The workload is responsible for running batchSize iterations on each call
     * @param <T> caller specific context data
     * @return the result of the measurement
     */
    public static <T> Measurement<T> regressionCheck(WorkerWorkload<T> workload, String metricName,
                                                boolean useConfigArgs, int warmUpCount,
                                                int numberOfCalls, int threadCount, int batchSize) {
        return regressionCheck(null, workload, metricName, useConfigArgs, 0L, warmUpCount, numberOfCalls, threadCount, batchSize);

    }

    /**
     * Measure the performance of a workload. The returned {@link Measurement} object contains the results of the measurement.
     *
     * @param workload the actual workload being measured
     * @param metricName the name of the test used as a key into performance data {@link PerformanceProfileStore#PERFDATAFILENAME}
     * @param useConfigArgs if true read test arguments from a file ({@link PerformanceProfileStore#getTestArgs(String)}
     * @param maxTestTime Abort the measurement if this time (in msecs) is exceeded TODO
     * @param warmUpCount Number of iterations of the workload to run before starting the measurement
     * @param numberOfCalls Number of workload iterations (workload is called in batchSize batches until numberOfCalls is reached)
     * @param threadCount Number of threads used to complete the workload
     * @param batchSize The workload is responsible for running batchSize iterations on each call
     * @param <T> caller specific context data
     * @return the result of the measurement
     */
    public static <T> Measurement<T> regressionCheck(WorkerWorkload<T> workload, String metricName,
                                                boolean useConfigArgs, long maxTestTime, int warmUpCount,
                                                int numberOfCalls, int threadCount, int batchSize) {
        return regressionCheck(null, workload, metricName, useConfigArgs, maxTestTime, warmUpCount, numberOfCalls, threadCount, batchSize);

    }

    /**
     * Measure the performance of a workload. The returned {@link Measurement} object contains the results of the measurement.
     *
     * @param lifecycle lifecycle calls during the measurement
     * @param workload the actual workload being measured
     * @param metricName the name of the test used as a key into performance data {@link PerformanceProfileStore#PERFDATAFILENAME}
     * @param useConfigArgs if true read test arguments from a file ({@link PerformanceProfileStore#getTestArgs(String)}
     * @param maxTestTime Abort the measurement if this time (in msecs) is exceeded TODO
     * @param warmUpCount Number of iterations of the workload to run before starting the measurement
     * @param numberOfCalls Number of workload iterations (workload is called in batchSize batches until numberOfCalls is reached)
     * @param threadCount Number of threads used to complete the workload
     * @param batchSize The workload is responsible for running batchSize iterations on each call
     * @param <T> caller specific context data
     * @return the result of the measurement
     */
    public static <T> Measurement<T> regressionCheck(WorkerLifecycle lifecycle, WorkerWorkload<T> workload, String metricName,
                                                boolean useConfigArgs, long maxTestTime, int warmUpCount,
                                                int numberOfCalls, int threadCount, int batchSize) {
        if (useConfigArgs) {
            String[] xargs = getTestArgs(metricName);

            maxTestTime = getArg(metricName, xargs, 0, maxTestTime, Long.class);
            warmUpCount = getArg(metricName, xargs, 1, warmUpCount, Integer.class);
            numberOfCalls = getArg(metricName, xargs, 2, numberOfCalls, Integer.class);
            threadCount = getArg(metricName, xargs, 3, threadCount, Integer.class);
            batchSize = getArg(metricName, xargs, 4, batchSize, Integer.class);
        }

        Measurement<T> opts = new Measurement<T>(maxTestTime, threadCount, numberOfCalls, batchSize).measure(lifecycle, workload);
        StringBuilder sb = new StringBuilder();

        opts.setRegression(!PerformanceProfileStore.checkPerformance(sb, metricName, (float) opts.getThroughput(), true));
        sb.append(String.format(" %d iterations using %d threads with a batch size of %d (warmup: %d error count: %d, tot millis: %d throughput: %f)",
                opts.getNumberOfCalls(), opts.getNumberOfThreads(), opts.getBatchSize(),
                opts.getNumberOfWarmupCalls(), opts.getNumberOfErrors(), opts.getTotalMillis(), opts.getThroughput()));
        opts.setInfo(sb.toString());

        return opts;
    }

    /**
     * Convert a String to another type
     *
     * @param metricName the name of the test to use if there was a data format error
     * @param args arguments returned from a prior call to {@link PerformanceProfileStore#getTestArgs(String)}
     * @param index index into the args array to the value to be converted
     * @param defaultValue default value if the index is out of range
     * @param argClass class type to convert the value to (which must have a constructor that takes a String value)
     * @param <T> the type which args[index] should be converted to
     * @return the converted value
     */
    public static <T> T getArg(String metricName, String[] args, int index, T defaultValue, Class<T> argClass)  {
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
     * Lookup any configured test arguments {@link PerformanceProfileStore#PERFARGSFILENAME}
     *
     * @param metricName the name of the test
     * @return any test arguments or an empty array if there are none
     */
    public static String[] getTestArgs(String metricName) {
        String argsString = System.getProperty(metricName + ".args");

        if (argsString == null)
            argsString = metrics.testArgs.getProperty(metricName, "");

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
    public static Map<String, Float> getMatchingMetrics(String pattern) {
        Map<String, Float> matches = new HashMap<>();
        Enumeration e = metrics.data.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            if (key.matches(pattern))
                matches.put(key, Float.valueOf(metrics.data.getProperty(key))); // data must contain numbers
        }

        return matches;
    }
}