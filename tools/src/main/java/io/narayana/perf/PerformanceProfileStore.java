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


    public final static Float DEFAULT_VARIANCE = 1.1F; // percentage variance that can be tolerated
    public final static String PERFDATAFILENAME = "PerformanceProfileStore.last";

    private final static String BASE_DIR = System.getProperty(BASE_DIRECTORY_PROPERTY);
    private final static boolean PERSIST_DATA = (BASE_DIR != null); //Boolean.parseBoolean(System.getProperty(ENABLE_CHECKS_PROPNAME));

    private final static PerformanceProfileStore metrics = new PerformanceProfileStore();

    private Properties data;
    private File dataFile;
    private float variance;

    private static float getMinVariance() {
        return Float.parseFloat(System.getProperty(DEFAULT_VARIANCE_PROPERTY_NAME, DEFAULT_VARIANCE.toString()));
    }

    public PerformanceProfileStore() {
        this(getMinVariance());
    }

    public PerformanceProfileStore(float variance) {
        this.variance = variance;
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
    }

    float getMetric(String name, float defaultValue) {
        return Float.parseFloat(data.getProperty(name, Float.toString(defaultValue)));
    }

    public boolean updateMetric(String metricName, Float metricValue) {
        return updateMetric(metricName, metricValue, false);
    }

    public boolean updateMetric(String metricName, Float metricValue, boolean largerIsBetter) {
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

        return isWithinTolerance(metricValue, canonicalValue, variance, largerIsBetter);
    }

    public static boolean checkPerformance(String performanceName, float operationDuration) throws IOException {
        return checkPerformance(performanceName, operationDuration, false);
    }

    public static boolean checkPerformance(String performanceName, float operationDuration, boolean largerIsBetter)
            throws IOException {
        return metrics.updateMetric(performanceName, operationDuration, largerIsBetter);
    }

    boolean isWithinTolerance(Float metricValue, Float canonicalValue, Float variance, boolean largerIsBetter) {
        Float headRoom = Math.abs(canonicalValue * (variance - 1));
        boolean within;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        System.out.printf("actual %f versus best %f: variance %f: head room: %f biggerBetter=%b ok=%b%n",
                metricValue, canonicalValue, variance, headRoom, largerIsBetter, within);

        return within;
    }

    boolean isBetter(Float metricValue, Float canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }
}
