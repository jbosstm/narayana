/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import com.arjuna.ats.jta.utils.JTAHelper;

public class Synchronization implements jakarta.transaction.Synchronization
{

    public void beforeCompletion ()
    {
	System.out.println("beforeCompletion called");
    }

    public void afterCompletion (int status)
    {
	System.out.println("afterCompletion called: "+JTAHelper.stringForm(status));
    }
 
}