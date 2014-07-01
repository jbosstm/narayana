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

package io.narayana.perf;

import java.io.*;
import java.util.Properties;

public class PerformanceProfileStore
{
    public final static String DEFAULT_VARIANCE_PROPERTY_NAME = "io.narayana.perf.PerformanceVariance";
    public final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";

    public static final String FAIL_ON_PERF_REGRESSION_PROP = "io.narayana.perf.failonregression";

    private static final boolean DEFAULT_FAIL_ON_REGRESSION = false;

    public final static Float DEFAULT_VARIANCE = 1.1F; // percentage _variance that can be tolerated
    public final static String PERFDATAFILENAME = "PerformanceProfileStore.last";

    private final static String BASE_DIR = System.getProperty(BASE_DIRECTORY_PROPERTY);
    private final static boolean PERSIST_DATA = (BASE_DIR != null); // if false then disable regression checks
    private static boolean failOnRegression = isFailOnRegression();

    private final static PerformanceProfileStore metrics = new PerformanceProfileStore();

    private Properties data;
    private File dataFile;
    private float _variance;

    private static float getMinVariance() {
        return Float.parseFloat(System.getProperty(DEFAULT_VARIANCE_PROPERTY_NAME, DEFAULT_VARIANCE.toString()));
    }

    public static boolean isFailOnRegression() {
        return System.getProperty(FAIL_ON_PERF_REGRESSION_PROP) == null ?  DEFAULT_FAIL_ON_REGRESSION :
            Boolean.getBoolean(FAIL_ON_PERF_REGRESSION_PROP);
    }

    public static float getVariance() {
        return metrics._variance;
    }

    public PerformanceProfileStore() {
        this(getMinVariance());
    }

    public PerformanceProfileStore(float variance) {
        this._variance = variance;
        data = new Properties();

        if (PERSIST_DATA) {
            if (BASE_DIR == null)
                throw new RuntimeException(BASE_DIRECTORY_PROPERTY + " property not set - cannot find performance test profiles!");

            dataFile = new File(BASE_DIR + File.separator + PERFDATAFILENAME);

            try {
                if (!dataFile.exists())
                    dataFile.createNewFile();

                InputStream is = new FileInputStream(dataFile);

                data.load(is);
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Cannot load previous performance profile", e);
            }
        }

        if (!failOnRegression)
            System.out.printf("PerformanceProfileStore: Regression checks are disabled%n");
    }

    float getMetric(String name, float defaultValue) {
        return Float.parseFloat(data.getProperty(name, Float.toString(defaultValue)));
    }

    public boolean updateMetric(String metricName, Float metricValue) {
        return updateMetric(_variance, metricName, metricValue, false);
    }

    public boolean updateMetric(String metricName, Float metricValue, boolean largerIsBetter) {
        return updateMetric(_variance, metricName, metricValue, largerIsBetter);
    }

    public boolean updateMetric(float variance, String metricName, Float metricValue, boolean largerIsBetter) {
        Float canonicalValue =  getMetric(metricName, metricValue);

        boolean better = isBetter(metricValue, canonicalValue, largerIsBetter);

        if (!data.containsKey(metricName) || better) {
            data.put(metricName, Float.toString(metricValue));
            if (PERSIST_DATA) {
                try {
                    data.store(new FileOutputStream(dataFile), "Performance profile (time in milli-seconds)");
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

    public static boolean checkPerformance(String performanceName, float variance, float operationDuration) throws IOException {
        return checkPerformance(performanceName, variance, operationDuration, false);
    }

    public static boolean checkPerformance(String performanceName, float variance, float operationDuration, boolean largerIsBetter)
            throws IOException {
        return metrics.updateMetric(variance, performanceName, operationDuration, largerIsBetter);
    }

    boolean isWithinTolerance(String metricName, Float metricValue, Float canonicalValue, Float variance, boolean largerIsBetter) {
        Float headRoom = Math.abs(canonicalValue * (variance - 1));
        boolean within;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        boolean ok =  within || !failOnRegression;

        System.out.printf("%s: actual %f versus best %f: _variance %f: head room: %f biggerBetter=%b within=%b (persist=%b failOnRegression=%b res=%b)%n",
                metricName, metricValue, canonicalValue, variance, headRoom, largerIsBetter, within, PERSIST_DATA, failOnRegression, ok);

        return ok;
    }

    boolean isBetter(Float metricValue, Float canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }
}
