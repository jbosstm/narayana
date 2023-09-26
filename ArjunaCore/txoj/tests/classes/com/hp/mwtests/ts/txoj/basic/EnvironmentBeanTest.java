/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.basic;

import java.util.HashMap;

import org.junit.Test;

import com.arjuna.ats.txoj.common.TxojEnvironmentBean;

/**
 * Unit tests for EnvironmentBean classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void testTxojEnvironmentBean() throws Exception {
        HashMap map;
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new TxojEnvironmentBean());
    }
}