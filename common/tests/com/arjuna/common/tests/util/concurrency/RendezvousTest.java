/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
* $Id: RendezvousTest.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.tests.util.concurrency;

import com.arjuna.common.util.concurrency.Rendezvous;

/**
 * TODO
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class RendezvousTest {

    public static final int CONSUMER_ID = 0;
    public static final int PRODUCER_ID = 1;

    public static final int NR_THREADS = 2;
    public static final int RENDEZVOUS_SIZE = 2;

    Thread[] threads = new Thread[NR_THREADS];
    Rendezvous rendezvous = new Rendezvous(RENDEZVOUS_SIZE);

    public static void main(String[] args)
    {
        new RendezvousTest().go();
    }

    public void go() {
//        for (int i = 0; i < NR_THREADS; i++)
//        {
//            threads[i] = new RendezvousThread(rendezvous);
//            threads[i].start();
//        }
        new ConsumerThread(rendezvous).start();
        new ProducerThread(rendezvous).start();
    }

}


class ConsumerThread extends Thread {

    private Rendezvous r = null;
    private static int threadCounter = 0;
    private int threadNumber = ++threadCounter;

    public ConsumerThread(Rendezvous r)
    {
        this.r = r;
        this.setName("ConsumerThread-" + threadNumber);
    }

    public void run() {
        int counter = 0;
        do {
            System.out.println(this + " entering rendezvous ---- # " + ++counter);
            Object object = r.enter(RendezvousTest.CONSUMER_ID, null);
            System.out.println(this + " consumed object " + object);
            System.out.println(this + " left rendezvous     ---- # " + counter);
//            try {
//                Thread.sleep((long) (Math.random() * 50.0));
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
//            }
        } while (true);
    }
}

class ProducerThread extends Thread {

    private Rendezvous r = null;
    private static int threadCounter = 0;
    private int threadNumber = ++threadCounter;

    public ProducerThread(Rendezvous r)
    {
        this.r = r;
        this.setName("ProducerThread-" + threadNumber);
    }

    public void run() {
        int counter = 0;
        do {
            System.out.println(this + " entering rendezvous ---- # " + ++counter);
            String object = "TOKEN-" + counter;
            System.out.println(this + " produces object " + object);
            r.enter(RendezvousTest.PRODUCER_ID, object);
            System.out.println(this + " left rendezvous     ---- # " + counter);
//            try {
//                Thread.sleep((long) (Math.random() * 500.0));
//            } catch (InterruptedException e) {
//                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
//            }
        } while (true);
    }

}
