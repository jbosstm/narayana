/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.utils;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
     * Lock for synchronizing access to the WeakHashMap
     */
    private static final Lock lock = new ReentrantLock();

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

        lock.lock();
        try {
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
        } finally {
            lock.unlock();
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