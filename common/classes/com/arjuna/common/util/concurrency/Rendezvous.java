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
* Rendezvous.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* $Id: Rendezvous.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.common.util.concurrency;


/**
 * TODO
 *
 * @author  Thomas Rischbeck <thomas.rischbeck@arjuna.com>
 * @version $Revision: 2342 $
 */
public class Rendezvous {

    public interface RendezvousFunction {
        public void rendezvousFunction(Object[] inObjects, Object[] outObjects);
    }

    public static class Rotator implements RendezvousFunction {
        public void rendezvousFunction(Object[] inObjects, Object[] outObjects)
        {
            int length = inObjects.length;
            for (int i = 0; i < length; i++)
            {
                outObjects[(i+i) % length] = inObjects[i];
            }

        }
    }

    private int size = 0;

    /**
     * counts how many threads are in teh rendezvous already
     */
    private int counter = 0;

    private Barrier barrier = null;

    private Object[] locks = null;

    private Object[] tokensIn = null;

    private Object[] tokensOut = null;

    private RendezvousFunction func = null;

    public Rendezvous(int size)
    {
        this.size = size;
        if (size < 2)
            throw new IllegalArgumentException("size must be a minimum of 2!");
        barrier = new Barrier(size);
        locks = new Object[size];
        tokensIn = new Object[size];
        tokensOut = new Object[size];
        for (int i = 0; i < size; i++)
            locks[i] = new Object();
        func = new Rotator();
    }


    public Object enter(int id, Object o) {
        if ((id < 0) || (id >= size) )
            throw new IllegalArgumentException("the id must be within the range 0 =< id < size : " + id);

        // only one entry per lock-id
        synchronized (locks[id])
        {
            tokensIn[id] = o;
            barrier.enter();
            synchronized(this)
            {
                counter++;
                if (counter == size)
                {
                    func.rendezvousFunction(tokensIn, tokensOut);
                    counter = 0;
                    notifyAll();
                }
                if (counter != 0)
                {
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException ie)
                    {
                        // ignore
                    }
                }
            }
            return tokensOut[id];
        }
    }

    public void setRendezvousFunction(RendezvousFunction func)
    {
        this.func = func;
    }
}
