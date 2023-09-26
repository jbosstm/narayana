/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SynchronizationRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since 3.0.
 */

public interface SynchronizationRecord extends Comparable
{
    public Uid get_uid ();

    public boolean beforeCompletion ();

    public boolean afterCompletion (int status);

    boolean isInterposed();
}