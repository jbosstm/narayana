/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.internal.jta.resources.errorhandlers.tibco;

public class ErrorHandlerUnitTest
{
    @Test
    public void test() throws Exception
    {
        tibco tc = new tibco();
        
        assertFalse(tc.notAProblem(new XAException(), false));
        assertEquals(tc.getXAResourceName(), "");
    }
}