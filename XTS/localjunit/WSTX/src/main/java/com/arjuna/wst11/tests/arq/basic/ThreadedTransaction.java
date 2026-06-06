/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ThreadedTransaction.java,v 1.1.24.1 2005/11/22 10:36:11 kconner Exp $
 */

package com.arjuna.wst11.tests.arq.basic;

import javax.inject.Named;

import com.arjuna.mw.wst11.UserTransaction;

class ThreadedObject extends Thread
{
    Exception exception;

    public ThreadedObject ()
    {
        exception = null;
    }

    public void run ()
    {
	try
	{
	    UserTransaction ut = UserTransaction.getUserTransaction();

	    ut.begin();

	    System.out.println("Thread "+Thread.currentThread()+" started "+ut);

	    Thread.yield();

	    System.out.println("\nThread "+Thread.currentThread()+" committing "+ut);

	    ut.commit();



	    Thread.yield();
	}
	catch (Exception ex)
	{
        ex.printStackTrace();
        exception = ex;
	}
    }

}

@Named
public class ThreadedTransaction
{

    public void testThreadedTransaction()
            throws Exception
    {
        int size = 10;
        ThreadedObject objs[] = new ThreadedObject[size];

        for (int i = 0; i < size; i++)
            objs[i] = new ThreadedObject();

        for (int j = 0; j < size; j++)
            objs[j].start();

        for (int k = 0; k < size; k++)
            objs[k].join();

        for (int k = 0; k < size; k++) {
            if (objs[k].exception != null) {
                throw objs[k].exception;
            }
        }
    }
}