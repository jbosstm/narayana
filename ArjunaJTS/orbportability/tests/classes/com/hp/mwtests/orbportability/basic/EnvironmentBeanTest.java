/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.orbportability.basic;

import org.junit.Test;

import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBean;

/**
 * Unit tests for EnvironmentBean classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void testOrbPortabilityEnvironmentBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new OrbPortabilityEnvironmentBean());
    }
}