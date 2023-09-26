/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

/**
 * The BasicStore provides core methods that all implementations MUST provide.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public interface BaseStore
{
    /**
     * @return the "name" of the object store. Where in the hierarchy it appears, e.g., /ObjectStore/MyName/...
     */

    public String getStoreName ();

    public void start();

    public void stop();
}