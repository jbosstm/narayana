/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.resources;

import javax.transaction.xa.XAException;

public interface XAResourceMap
{
    public boolean notAProblem (XAException ex, boolean commit);

    public String getXAResourceName ();
}