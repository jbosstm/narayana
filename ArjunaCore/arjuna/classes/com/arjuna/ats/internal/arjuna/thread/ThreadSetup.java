/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.thread;

/**
 * Implementations of this class can be registered with the low-level
 * transaction-threading system to set up any thread data that is needed
 * for thread-to-transaction tracking to be done correctly. This level of
 * indirection allows us to keep the core neutral to such issues (which tend
 * to be ORB specific), so that it can be used "raw".
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ThreadSetup.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public interface ThreadSetup
{

    public void setup ();

}