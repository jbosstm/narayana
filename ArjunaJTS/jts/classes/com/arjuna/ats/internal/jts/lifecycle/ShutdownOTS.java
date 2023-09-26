/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.lifecycle;

public class ShutdownOTS extends com.arjuna.orbportability.oa.PreShutdown
{

    public ShutdownOTS ()
    {
	super("ShutdownOTS");
    }

    public void work ()
    {
	com.arjuna.ats.internal.jts.OTSImpleManager.purge();
    }
 
}