/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.Utils;

import java.io.*;
import java.util.*;

/**
 * Maintain performance data and check for regressions.
 *
 * Performance data and configuration is stored in a directory named by the system property {@link PerformanceProfileStore#BASE_DIRECTORY_PROPERTY}:
 * - PerformanceProfileStore.last holds the best performance run keyed by the name of the test
 * - PerformanceProfileStore.variance contains the variance for a test (keyed by test name) or, if not present, then the default variance
 *   (eg 1.1 indicates a variance of lest than 10%)
 * - PerformanceProfileStore.args contains any arguments required by a test keyed by test name with value a comma separated string
 *   (the configured values for arguments can be overridden by setting a system property called "testname.args" to the new value)
 *
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

    float getMetric(String name, float defaultValue) {
        return Float.parseFloat(data.getProperty(name, Float.toString(defaultValue)));
    }

    public boolean updateMetric(String metricName, Float metricValue) {
        return updateMetric(_variance, metricName, metricValue, false);
    }

    public boolean updateMetric(String metricName, Float metricValue, boolean largerIsBetter) {
        return updateMetric(getVariance(metricName), metricName, metricValue, largerIsBetter);
    }

    public boolean updateMetric(float variance, String metricName, Float metricValue, boolean largerIsBetter) {
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

        return isWithinTolerance(metricName, metricValue, canonicalValue, variance, largerIsBetter);
    }

    public static boolean checkPerformance(String performanceName, float metricValue) throws IOException {
        return checkPerformance(performanceName, metricValue, false);
    }

    public static boolean checkPerformance(String performanceName, float metricValue, boolean largerIsBetter)
            throws IOException {
        return metrics.updateMetric(performanceName, metricValue, largerIsBetter);
    }

    public static boolean checkPerformance(String performanceName, float variance, float metricValue)
            throws IOException {
        return checkPerformance(performanceName, variance, metricValue, false);
    }

    public static boolean checkPerformance(String performanceName, float variance, float metricValue,
                                           boolean largerIsBetter) throws IOException {
        return metrics.updateMetric(variance, performanceName, metricValue, largerIsBetter);
    }

    boolean isWithinTolerance(String metricName, Float metricValue, Float canonicalValue, Float variance,
                              boolean largerIsBetter) {
        Float headRoom = Math.abs(canonicalValue * (variance - 1));
        boolean within;
        Float difference = (metricValue - canonicalValue) / canonicalValue * 100;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        boolean ok =  within || !failOnRegression;

        System.out.printf("%s %s: %f%% performance %s (%f versus %f) (variance=%f headroom=%f)%n",
                metricName, ok ? "Passed" : "Failed", difference,
                within ? "difference" : "regression", metricValue, canonicalValue, variance, headRoom);

        return ok;
    }

    boolean isBetter(Float metricValue, Float canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }

    /**
     * Convert a String to another type
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
}
