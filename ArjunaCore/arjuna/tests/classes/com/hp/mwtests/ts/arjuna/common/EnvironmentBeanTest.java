/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.common;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;

/**
 * Unit tests for EnvironmentBean classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void testCoordinatorEnvironmentBean() throws Exception {
    	CoordinatorEnvironmentBean coordinatorEnvironmentBean = new CoordinatorEnvironmentBean();
    	coordinatorEnvironmentBean.setAllowCheckedActionFactoryOverride(true);
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(coordinatorEnvironmentBean);
    }

    @Test
    public void testObjectStoreEnvironmentBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new ObjectStoreEnvironmentBean());
    }

    @Test
    public void testRecoveryEnvironmentBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new RecoveryEnvironmentBean());
    }

    @Test
    public void testCoreEnvironmentBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new CoreEnvironmentBean());
    }
}