/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.resources.errorhandlers;

import javax.transaction.xa.XAException;

import com.arjuna.ats.jta.resources.XAResourceMap;

public class tibco implements XAResourceMap
{

    public boolean notAProblem (XAException ex, boolean commit)
    {
	return false;
    }

    public String getXAResourceName ()
    {
	return "";
    }

}