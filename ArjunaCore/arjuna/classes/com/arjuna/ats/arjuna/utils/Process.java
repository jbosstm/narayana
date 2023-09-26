/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.utils;

/**
 * Provides a configurable way to get a unique process id.
 * 
 * @version $Id: Process.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public interface Process
{

    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     * @since JTS 2.1.
     */

    public int getpid ();

}