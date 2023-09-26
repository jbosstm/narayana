/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Reapable.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.0.
 */

public interface Reapable
{

    boolean running();

    boolean preventCommit();

    int cancel();

    Uid get_uid();

    default void recordStackTraces() {
        // default null-op
    }

    default void outputCapturedStackTraces() {
        // default null-op
    }
    
}