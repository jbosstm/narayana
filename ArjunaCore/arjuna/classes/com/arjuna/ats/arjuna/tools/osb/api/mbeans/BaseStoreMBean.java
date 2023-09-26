/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

// MBean interface corresponding to com.arjuna.ats.arjuna.objectstore.BaseStore
public interface BaseStoreMBean
{
    public String getStoreName ();
    public void start();
    public void stop();
}