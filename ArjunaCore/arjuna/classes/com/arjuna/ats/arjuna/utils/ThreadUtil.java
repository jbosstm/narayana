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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
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
    // Ideally a WeakHashMap would be used here, but unfortunately it's not thread-safe
    // Would need to synchronize it, as even gets have to be treated as writes since they remove stale entries
    private static final ConcurrentWeakHashMap THREAD_ID = new ConcurrentWeakHashMap();

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
        return THREAD_ID.get(thread);
    }

    /**
     * Get the next thread id to use.
     * @return The next thread id.
     */
    private static String getNextId()
    {
        return Long.toHexString(id.incrementAndGet());
    }

    // --- //

    private static final class ConcurrentWeakHashMap
    {
        private final ConcurrentHashMap<WeakKey<Thread>, String> map = new ConcurrentHashMap<>();
        private final ReferenceQueue<Thread> refQueue = new ReferenceQueue<>();

        public String get(Thread thread)
        {
            if (thread == null)
            {
                throw new NullPointerException("null thread");
            }
            purgeStaleKeys();

            // Should just call map.computeIfAbsent(), but do it old style to avoid creating a lambda
            String newValue, value = map.get(thread);
            if (value == null)
            {
                value = map.putIfAbsent(new WeakKey<>(thread, refQueue), newValue = getNextId());
                return value == null ? newValue : value;
            }
            return value;
        }

        private void purgeStaleKeys()
        {
            for(Reference<? extends Thread> ref; (ref = refQueue.poll()) != null; map.remove(ref))
            {
            }
        }

        private static class WeakKey<K> extends WeakReference<K>
        {
            private final int hashCode;

            private WeakKey(K key, ReferenceQueue<K> queue)
            {
                super(key, queue);
                hashCode = key.hashCode();
            }

            public int hashCode()
            {
                return hashCode;
            }

            public boolean equals(Object other)
            {
                if (other == this)
                {
                    return true;
                }
                else if (!(other instanceof WeakKey))
                {
                    return false;
                }
                else
                {
                    Object referent = this.get();
                    return referent != null && referent == ((WeakKey)other).get();
                }
            }
        }
    }
}
