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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

// TODO use  tools/src/main/java/io/narayana/perf/PerformanceProfileStore instead
public class PerformanceProfileStore
{
    public static final String DEFAULT_VARIANCE_PROPERTY_NAME = "io.narayana.perf.PerformanceVariance";
    public static Float DEFAULT_VARIANCE = 1.1F; // percentage variance that can be tolerated
    public static String PERFDATAFILENAME = "PerformanceProfileStore.last";

    private final static String BASE_DIRECTORY_PROPERTY = "performanceprofilestore.dir";
    private final static String baseDir = System.getProperty(BASE_DIRECTORY_PROPERTY);
    private static PerformanceProfileStore metrics;

    private Properties data;
    private File dataFile;
    private float variance;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Calendar cal = Calendar.getInstance();

    private static float getMinVariance() {
        return Float.parseFloat(System.getProperty(DEFAULT_VARIANCE_PROPERTY_NAME, DEFAULT_VARIANCE.toString()));
    }

    public PerformanceProfileStore() throws IOException {
        this(getMinVariance());
    }

    public PerformanceProfileStore(float variance) throws IOException {
        if (baseDir == null)
            throw new RuntimeException(BASE_DIRECTORY_PROPERTY + " property not set - cannot find performance test profiles!");

        this.variance = variance;
        data = new Properties();
        dataFile = new File(baseDir + File.separator + PERFDATAFILENAME);

        if (!dataFile.exists()) {
            dataFile.createNewFile();
        }

        InputStream is = new FileInputStream(dataFile);

        data.load(is);
        is.close();
    }

    float getMetric(String name, float defaultValue) {
        return Float.parseFloat(data.getProperty(name, Float.toString(defaultValue)));
    }

    float getMetric(String name) {
        return getMetric(name, (float) 0.0);
    }

    public boolean updateMetric(String metricName, Float metricValue) throws IOException {
         return updateMetric(metricName, metricValue, false);
    }

    public boolean updateMetric(String metricName, Float metricValue, boolean largerIsBetter) throws IOException {
        Float canonicalValue =  getMetric(metricName, metricValue);

        boolean better = isBetter(metricValue, canonicalValue, largerIsBetter);

        if (!data.containsKey(metricName) || better) {
            data.put(metricName, Float.toString(metricValue));
            data.store(new FileOutputStream(dataFile), dateFormat.format(cal.getTime()).toString() +
                ": Performance profile (time in milli-seconds)");
        }

        return isWithinTolerance(metricName, metricValue, canonicalValue, variance, largerIsBetter);
    }

    public static PerformanceProfileStore getMetrics() throws IOException {
        if (metrics == null)
            metrics = new PerformanceProfileStore();

        return metrics;
    }

    public static boolean checkPerformance(String performanceName, float operationDuration) {
        return checkPerformance(performanceName, operationDuration, false);
    }

	public static boolean checkPerformance(String performanceName, float operationDuration, boolean largerIsBetter)
	{
		try {
          return getMetrics().updateMetric(performanceName, operationDuration, largerIsBetter);
		} catch (Exception e)
		{
			System.err.println("checkPerformance: " + e);
			e.printStackTrace(System.err);

            return false;
		}
	}

    boolean isWithinTolerance(String metricName, Float metricValue, Float canonicalValue, Float variance, boolean largerIsBetter) {
        Float headRoom = Math.abs(canonicalValue * (variance - 1));

        boolean within;

        if (largerIsBetter)
            within = (metricValue >= canonicalValue - headRoom);
        else
            within = (metricValue <= canonicalValue + headRoom);

        System.out.printf("%s: actual %f versus best %f: variance %f: head room: %f biggerBetter=%b ok=%b%n",
                metricName, metricValue, canonicalValue, variance, headRoom, largerIsBetter, within);

        return within;
    }

    boolean isBetter(Float metricValue, Float canonicalValue, boolean largerIsBetter) {
        if (largerIsBetter)
            return (metricValue > canonicalValue);
        else
            return (metricValue < canonicalValue);
    }
}
