/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.common;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.common.BasicMutex;

import static org.junit.Assert.*;

class MutexThread extends Thread
{
    public MutexThread (BasicMutex share)
    {
        _mutex = share;
    }
    
    public void run ()
    {
        if (_mutex.lock() == BasicMutex.LOCKED)
        {
            for (int i = 0; i < 1000; i++)
            {
                // do some work
                
                System.currentTimeMillis();
            }
            
            _mutex.unlock();
        }

        _done = true;
    }
    
    public boolean valid ()
    {
        return _done;
    }
    
    private BasicMutex _mutex;
    private boolean _done = false;
}

public class MutexUnitTest
{
    @Test
    public void testBasicMutex () throws Exception
    {
        BasicMutex mx = new BasicMutex();
        
        mx.lock();
        
        mx.unlock();
    }
    
    @Test
    public void testThreaded () throws Exception
    {
        BasicMutex mx = new BasicMutex();
        MutexThread mt1 = new MutexThread(mx);
        MutexThread mt2 = new MutexThread(mx);
        
        mt1.run();
        mt2.run();
        
        try
        {
            mt1.join();
            mt2.join();
        }
        catch (final Throwable ex)
        {
            fail();
        }
        
        assertTrue(mt1.valid());
        assertTrue(mt2.valid());
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        BasicMutex mx = new BasicMutex();

        assertEquals(mx.unlock(), BasicMutex.ERROR);
    }
    
    @Test
    public void testReentrantMutex () throws Exception
    {
        BasicMutex mx = new BasicMutex();
        
        mx.lock();
        
        assertEquals(mx.tryLock(), BasicMutex.WOULD_BLOCK);
        
        mx.unlock();
        
        mx = new BasicMutex(true);
        
        mx.lock();
        
        assertEquals(mx.tryLock(), BasicMutex.LOCKED);
        
        mx.unlock();
        mx.unlock();
    }
}
