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

import io.narayana.perf.RegressionChecker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NarayanaComparison extends Product {
    final static String outerClassName =  ProductComparison.class.getName();
    private static Map<String, Double> productMetrics;
    final static String nameOfMetric = outerClassName + METHOD_SEP + "Narayana";

    @Test
    public void testNarayana() throws SystemException {
        runTest(new ProductInterface() {
            UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();
            TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            @Override
            public UserTransaction getUserTransaction() throws SystemException {
                return ut;
            }

            @Override
            public TransactionManager getTransactionManager() {
                return tm;
            }

            @Override
            public String getNameOfMetric() {
                return nameOfMetric;
            }

            @Override
            public void init() {
            }

            @Override
            public void fini() {
            }
        });
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        productMetrics = new RegressionChecker().getMatchingMetrics(ProductComparison.getMetricPrefix() + ".*");
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Map<String, Double> newMetrics = new RegressionChecker().getMatchingMetrics(ProductComparison.getMetricPrefix() + ".*");
        Map<String, String> failures = new HashMap<>(); // metric name -> reason for failure
        Double variance = 1.1;
        StringBuilder sb = new StringBuilder("Performance Regressions:%n");

        for (Map.Entry<String, Double> entry : newMetrics.entrySet()) {
            String metricName = entry.getKey();

            if (!productMetrics.containsKey(metricName))
                continue; // there is no previous value to use for regression checks

            Double canonicalValue = productMetrics.get(metricName); // the previous (best) value
            Double headRoom = Math.abs(productMetrics.get(metricName) * (variance - 1)); // the leeway either side of the best value
            Double difference = (entry.getValue() - canonicalValue) / canonicalValue * 100;
            boolean withinTolerance = (entry.getValue() >= canonicalValue - headRoom);

            if (!withinTolerance) {
                String s = String.format("%s: %f%% performance %s (%f versus %f) (variance=%f headroom=%f)%n",
                    metricName, difference,  "regression", entry.getValue(), canonicalValue, variance, headRoom);

                failures.put(metricName, s);
                sb.append(s);
            }

            assertEquals(sb.toString(), 0, failures.size());
        }
    }
}
