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

import org.junit.Test;

import java.math.BigInteger;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;

public class PerformanceTest {
    private BigInteger factorial(int num) {
        BigInteger serialFac = BigInteger.ONE;

        for (int i = 2; i <= num; i++) {
            serialFac = serialFac.multiply(BigInteger.valueOf(i));
        }

        return serialFac;
    }

    @Test
    public void testPerformanceTester() {
        PerformanceTester<BigInteger> tester = new PerformanceTester<BigInteger>(10, 10);
        FactorialWorker worker = new FactorialWorker();

        int threadCount = 10;
        int numberOfCalls = 10000;
        // 1000000!: (total time: 78635 ms versus 1629870 ms) so dont make numberOfCalls to big

        Result<BigInteger> opts = new Result<BigInteger>(threadCount, numberOfCalls);

        try {
            Result<BigInteger> res = tester.measureThroughput(worker, opts);
            Set<BigInteger> subFactorials = res.getContexts();
            BigInteger fac = BigInteger.ONE;

            for (BigInteger bigInteger : subFactorials)
                fac = fac.multiply(bigInteger);

            long start = System.nanoTime();
            BigInteger serialFac = factorial(opts.getNumberOfCalls());
            long millis = (System.nanoTime() - start) / 1000000L;

            System.out.printf("Test performance for %d!: %d calls / second (total time: %d ms versus %d ms)%n",
                    opts.getNumberOfCalls(), opts.getThroughput(), opts.getTotalMillis(), millis);

            assertTrue("Factorials not equal", serialFac.equals(fac));

            assertTrue("init method not called", worker.getInitTimemillis() != -1);
            assertTrue("doWork method not called", worker.getWorkTimeMillis() != -1);
            assertTrue("fini method not called", worker.getFiniTimeMillis() != -1);

            assertTrue("init method called after work method", worker.getInitTimemillis() <= worker.getWorkTimeMillis());
            assertTrue("work method called after fini method", worker.getWorkTimeMillis() <= worker.getFiniTimeMillis());

        } finally {
            tester.fini();
        }
    }
}
