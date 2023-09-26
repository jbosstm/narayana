/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.perf;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class FactorialWorker implements Worker<BigInteger> {
    private AtomicInteger facBase = new AtomicInteger(0);
    private long initTimemillis = -1;
    private long workTimeMillis = -1;
    private long finiTimeMillis = -1;

    @Override
    public BigInteger doWork(BigInteger partialFac, int niters, Measurement<BigInteger> config) {
        int from = facBase.getAndAdd(niters);
        int to = from + niters;

        if (partialFac == null)
            partialFac = BigInteger.ONE;

        for (long i = from + 1; i <= to; i++)
            partialFac = partialFac.multiply(BigInteger.valueOf(i));

        workTimeMillis = System.currentTimeMillis();

        return partialFac;
    }

    @Override
    public void finishWork(Measurement<BigInteger> measurement) {
    }

    @Override
    public void init() {
        initTimemillis = System.currentTimeMillis();
    }

    @Override
    public void fini() {
        finiTimeMillis = System.currentTimeMillis();
    }

    public long getInitTimemillis() {
        return initTimemillis;
    }

    public long getWorkTimeMillis() {
        return workTimeMillis;
    }

    public long getFiniTimeMillis() {
        return finiTimeMillis;
    }
}