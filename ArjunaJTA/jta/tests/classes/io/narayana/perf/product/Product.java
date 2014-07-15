/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package io.narayana.perf.product;

import io.narayana.perf.Measurement;
import io.narayana.perf.PerformanceProfileStore;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Product {
    final protected static String METHOD_SEP = "_";
    protected final static Map<String, Measurement> measurements = new ConcurrentHashMap<String, Measurement>();

    protected static Measurement getMeasurement(String name) {
        return measurements.get(name);
    }

    protected static Integer getThroughput(String name) {
        Measurement measurement = measurements.get(name);

        return measurement == null ? null : measurement.getThroughput();
    }

    protected void runTest(ProductInterface prod) {
        int threads = 10;
        int batchSize = 100;
        int warmUpCount = 10;
        int numberOfTransactions = threads * batchSize;
        long maxTestTime = 0;
        boolean readTestArgsFromFile = true;

        ProductWorkload worker = new ProductWorkload(prod);

        Measurement measurement = PerformanceProfileStore.regressionCheck(
                worker, worker, prod.getNameOfMetric(), readTestArgsFromFile, maxTestTime, warmUpCount, numberOfTransactions, threads, batchSize);
        measurements.put(prod.getNameOfMetric(), measurement);

        Assert.assertEquals(0, measurement.getErrorCount());
        Assert.assertFalse(measurement.getInfo(), measurement.isRegression());

        System.out.printf("%s%n", measurement.getInfo());
    }
}
