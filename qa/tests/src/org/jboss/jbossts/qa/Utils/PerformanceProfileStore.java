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

public class PerformanceProfileStore
{
    public final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";

    public static final String FAIL_ON_PERF_REGRESSION_PROP = "io.narayana.perf.failonregression";

    private static final boolean DEFAULT_FAIL_ON_REGRESSION = false;

    public final static Float DEFAULT_VARIANCE = 1.1F; // percentage _variance that can be tolerated
    public final static String PERFDATAFILENAME = "PerformanceProfileStore.last";
    public final static String PERFVARIANCEFILENAME = "PerformanceProfileStore.variance";
    public final static String PERFARGSFILENAME = "PerformanceProfileStore.args";
    public final static String PROPFILE_COMMENT =
        "Performance profile. Format is testName=value where value is the metric (throughput or duration)";

    private final static String BASE_DIR = System.getProperty(BASE_DIRECTORY_PROPERTY);
    private static boolean failOnRegression = isFailOnRegression();

    private final static PerformanceProfileStore metrics = new PerformanceProfileStore();

    private Properties data;
    private Properties variances;
    private Properties testArgs;
    private File dataFile;
    private float _variance;

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
                throw new RuntimeException("Cannot load previous performance profile", e);
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
        Float canonicalValue =  getMetric(metricName, metricValue);

        boolean better = isBetter(metricValue, canonicalValue, largerIsBetter);

        if (!data.containsKey(metricName) || better) {
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

    public static boolean checkPerformance(String performanceName, float operationDuration) throws IOException {
        return checkPerformance(performanceName, operationDuration, false);
    }

    public static boolean checkPerformance(String performanceName, float operationDuration, boolean largerIsBetter)
            throws IOException {
        return metrics.updateMetric(performanceName, operationDuration, largerIsBetter);
    }

    public static boolean checkPerformance(String performanceName, float variance, float operationDuration)
        throws IOException {
        return checkPerformance(performanceName, variance, operationDuration, false);
    }

    public static boolean checkPerformance(String performanceName, float variance, float operationDuration,
        boolean largerIsBetter)
            throws IOException {
        return metrics.updateMetric(variance, performanceName, operationDuration, largerIsBetter);
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

    public static String[] getTestArgs(String metricName) {
        String[] args = metrics.testArgs.getProperty(metricName, "").split(",");
        List<String> list = new ArrayList<>(Arrays.asList(args));

        list.removeAll(Collections.singleton(""));

        return list.toArray(new String[list.size()]);
    }
}
