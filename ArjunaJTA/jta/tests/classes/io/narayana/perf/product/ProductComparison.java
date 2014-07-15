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

import io.narayana.perf.PerformanceProfileStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProductComparison extends Product {
    final private static String outerClassName =  ProductComparison.class.getName();

    private static Map<String, Float> oldMetrics;
    final static private String narayanaMetricName = outerClassName + METHOD_SEP + "Narayana";

    static String getMetricPrefix() {
        return outerClassName + METHOD_SEP;
    }

    private static Map<String, Float> getVariances(Map<String, Float> metrics, Float targetMetric) {
        Map<String, Float> variances = new HashMap<>();

        if (targetMetric == null)
            return variances;

        for (Map.Entry<String, Float> entry : metrics.entrySet()) {
            Float difference = (entry.getValue() - targetMetric) / targetMetric * 100;

            variances.put(entry.getKey(), difference);
        }

        return variances;
    }

    @BeforeClass
    public static void beforeClass() {
        oldMetrics = PerformanceProfileStore.getMatchingMetrics(ProductComparison.getMetricPrefix() + ".*");
    }

    @AfterClass
    public static void afterClass() {
        Map<String, Float> newMetrics = PerformanceProfileStore.getMatchingMetrics(ProductComparison.getMetricPrefix() + ".*");

        Map<String, Float> oldVariances = getVariances(oldMetrics, oldMetrics.get(narayanaMetricName));
        Map<String, Float> newVariances = getVariances(newMetrics, (float) getThroughput(narayanaMetricName));

        Map<String, String> failures = new HashMap<>(); // metric name -> reason for failure
        Float variance = 1.1F;

        StringBuilder sb = new StringBuilder("Performance Regressions:%n");

        for (Map.Entry<String, Float> entry : oldVariances.entrySet()) {
            String metricName = entry.getKey();

            if (!metricName.equals(narayanaMetricName) && newVariances.containsKey(metricName)) {
                Float oldVariance = entry.getValue(); // the previous value
                Float newVariance = newVariances.get(metricName);

                Float headRoom = Math.abs(oldVariance * (variance - 1)); // the leeway either side of the prev value
                Float difference = (oldVariance - newVariance) / newVariance * 100;

                boolean withinTolerance = (newVariance >= oldVariance - headRoom);

                if (!withinTolerance) {
                    String s = String.format("%s: %f%% performance regression (%f versus %f) (variance=%f headroom=%f)%n",
                            metricName, difference, newVariance, oldVariance, variance, headRoom);

                    failures.put(metricName, s);
                    sb.append(s);
                }
            }
        }

        assertEquals(sb.toString(), 0, failures.size());

/*        for (Map.Entry<String, Float> entry : newMetrics.entrySet()) {
            String metricName = entry.getKey();

            if (!oldMetrics.containsKey(metricName))
                continue; // there is no previous value to use for regression checks

            Float canonicalValue = oldMetrics.get(metricName); // the previous (best) value
            Float headRoom = Math.abs(oldMetrics.get(metricName) * (variance - 1)); // the leeway either side of the best value
            Float difference = (entry.getValue() - canonicalValue) / canonicalValue * 100;
            boolean withinTolerance = (entry.getValue() >= canonicalValue - headRoom);

            if (!withinTolerance) {
                String s = String.format("%s: %f%% performance %s (%f versus %f) (variance=%f headroom=%f)%n",
                    metricName, difference,  "regression", entry.getValue(), canonicalValue, variance, headRoom);

                failures.put(metricName, s);
                sb.append(s);
            }

            assertEquals(sb.toString(), 0, failures.size());
        }*/
    }

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
                return narayanaMetricName;
            }

            @Override
            public void init() {
            }

            @Override
            public void fini() {
            }
        });
    }

    @Test
    public void testJotm() {
        runTest(new ProductInterface() {
            org.objectweb.jotm.Jotm jotm;

            @Override
            public UserTransaction getUserTransaction() throws SystemException {
                return jotm.getUserTransaction();
/*                try {
                    return (UserTransaction) new InitialContext().lookup("UserTransaction");
                } catch (NamingException e) {
                    throw new SystemException(e.getMessage());
                }*/
            }

            @Override
            public TransactionManager getTransactionManager() {
                return jotm.getTransactionManager();
            }

            @Override
            public String getNameOfMetric() {
                return outerClassName + "_Jotm";
            }

            @Override
            public void init() {
                try {
                    jotm = new org.objectweb.jotm.Jotm(true, false);
                } catch (javax.naming.NamingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void fini() {
                org.objectweb.jotm.TimerManager.stop();
                jotm.stop();
            }
        });
    }

    @Test
    public void testBitronix() {
        runTest(new ProductInterface() {
            @Override
            public UserTransaction getUserTransaction() {
                return bitronix.tm.TransactionManagerServices.getTransactionManager();
            }

            @Override
            public TransactionManager getTransactionManager() {
                return bitronix.tm.TransactionManagerServices.getTransactionManager();
            }

            @Override
            public String getNameOfMetric() {
                return outerClassName + "_Bitronix";
            }

            @Override
            public void init() {
            }

            @Override
            public void fini() {
            }
        });
    }

    @Test
    public void testAtomikos() {
        runTest(new ProductInterface() {
            @Override
            public UserTransaction getUserTransaction() throws SystemException {
                return new com.atomikos.icatch.jta.UserTransactionImp();
            }

            @Override
            public TransactionManager getTransactionManager() {
                return com.atomikos.icatch.jta.TransactionManagerImp.getTransactionManager();
            }

            @Override
            public String getNameOfMetric() {
                return outerClassName + "_Atomkos";
            }
            @Override
            public void init() {
            }

            @Override
            public void fini() {
            }
        });
    }
}
