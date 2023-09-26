/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.basic;

import org.junit.Test;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;

/**
 * Unit tests for EnvironmentBean classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void testJTAEnvironmentBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new JTAEnvironmentBean());
    }
}