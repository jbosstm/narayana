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
package com.arjuna.ats.arjuna.utils;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides utilities to manage thread ids.
 */
public class ThreadUtil
{
    /**
     * The cached ID of the current thread.
     */
    private static final ThreadLocal<String> LOCAL_ID = new ThreadLocal<>();

    /**
     * The ID associated with the thread.
     */
    private static final WeakHashMap<Thread,String> THREAD_ID = new WeakHashMap<Thread,String>();
    /**
     * The thread id counter.
     */
    private static AtomicLong id = new AtomicLong();

    /**
     * Get the string ID for the current thread.
     * @return The thread id
     */
    public static String getThreadId()
    {
        return getThreadId(Thread.currentThread());
    }

    /**
     * Get the string ID for the specified thread.
     * @param thread The thread.
     * @return The thread id
     */
    public static String getThreadId(final Thread thread)
    {
        // fastpath - if we have cached the result in a thread local,
        // we can avoid lock contention on the map.
        if(thread == Thread.currentThread()) {
            String id = LOCAL_ID.get();
            if(id != null) {
                return id;
            }

        }

        synchronized (ThreadUtil.class) {
            final String id = THREAD_ID.get(thread);
            if (id != null) {
                if (thread == Thread.currentThread()) {
                    LOCAL_ID.set(id);
                }

                return id;
            }

            final String newId = getNextId();
            THREAD_ID.put(thread, newId);
            if (thread == Thread.currentThread()) {
                LOCAL_ID.set(newId);
            }
            return newId;
        }
    }

    /**
     * Get the next thread id to use.
     * @return The next thread id.
     */
    private static String getNextId()
    {
        return Long.toHexString(id.incrementAndGet());
    }
}
