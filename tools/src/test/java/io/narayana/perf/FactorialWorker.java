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
