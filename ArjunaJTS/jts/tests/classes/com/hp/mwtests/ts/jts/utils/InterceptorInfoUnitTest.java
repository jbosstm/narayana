/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.jts.common.InterceptorInfo;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class InterceptorInfoUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        assertEquals(InterceptorInfo.getNeedTranContext(), jtsPropertyManager.getJTSEnvironmentBean().isNeedTranContext());
        
        assertEquals(InterceptorInfo.getAlwaysPropagate(), jtsPropertyManager.getJTSEnvironmentBean().isAlwaysPropagateContext());
    }
}