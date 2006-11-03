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
* Barrier.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* $Id: Barrier.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.util.concurrency;

/**
 * TODO
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class Barrier {

    private int size = 0;
    private int counter = 0;

    private static final int COLLECTING = 0;
    private static final int OPENING = 1;

    private int state = COLLECTING;
    private Object entryLock = new Object();
    private Object openLock = new Object();

    /**
     * Create a new Barrier with the given size
     *
     * @param size
     */
    protected Barrier(int size)
    {
        if (size < 2)
            throw new IllegalArgumentException("size must be a minimum of 2!");
        this.size = size;
    }

    /**
     * add a token to the box and only return when all required tokens are there (like dataflow)
     * This method only returns once exactly size threads have entered and then all the size
     * threads are released simultaneously
     */
    protected synchronized void enter() {
        //synchronized (entryLock)
        {
            // wait until in COLLECTING state
            while (state != COLLECTING)
            try {
                wait();
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }

            // transition to OPENING state when the required number of
            // threads have entered the barrier ...
            counter++;
            if (size == counter)
            {
                state = OPENING;
                notifyAll();
            }
        }

        //synchronized (openLock)
        {
            // wait until in OPENING state
            while (state != OPENING)
            try {
                wait();
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }

            // transition to COLLECTING state once all the previously collected
            // threads have left ...
            counter--;
            if (counter == 0)
            {
                state = COLLECTING;
                notifyAll();
            }
        }
    }
}


