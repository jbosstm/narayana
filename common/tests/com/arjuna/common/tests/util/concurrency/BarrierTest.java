/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
/*
* BarrierTest.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* $Id: BarrierTest.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.tests.util.concurrency;

import com.arjuna.common.util.concurrency.Barrier;


/**
 * TODO
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class BarrierTest {

    public static final int NR_THREADS = 4;
    public static final int BARRIER_SIZE = 3;

    Thread[] threads = new Thread[NR_THREADS];
    TestBarrier barrier = new TestBarrier(BARRIER_SIZE);

    public static void main(String[] args)
    {
        new BarrierTest().go();
    }

    public void go() {
        for (int i = 0; i < NR_THREADS; i++)
        {
            threads[i] = new BarrierThread(barrier);
            threads[i].start();
        }
    }

}


class BarrierThread extends Thread {

    private TestBarrier b = null;
    private static int threadCounter = 0;
    private int threadNumber = ++threadCounter;

    public BarrierThread(TestBarrier b)
    {
        this.b = b;
        this.setName("BarrierThread-" + threadNumber);
    }

    public void run() {
        int counter = 0;
        do {
            System.out.println(this + " entering barrier ---- # " + ++counter);
            b.enter();
            System.out.println(this + " left barrier     ---- # " + counter);
            try {
                Thread.sleep((long) (Math.random() * 5000.0));
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        } while (true);
    }
}


class TestBarrier extends Barrier
{
	public TestBarrier(final int size)
	{
		super(size) ;
	}
	
    public void enter()
    {
    		super.enter() ;
    }
}
