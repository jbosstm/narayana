/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jdbc.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Hashtable;

import org.junit.Test;

import com.arjuna.ats.jdbc.common.JDBCEnvironmentBean;

/**
 * Unit tests for EnvironmentBean classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void testJDBCEnvironmentBean() throws Exception {
        // reflection won't work on this one :-(

        JDBCEnvironmentBean bean = new JDBCEnvironmentBean();

        bean.setIsolationLevel(2);
        assertEquals(2, bean.getIsolationLevel());

        Hashtable hashtable = new Hashtable();
        hashtable.put("testKey", "testValue");

        bean.setJndiProperties(hashtable);
        assertEquals(hashtable, bean.getJndiProperties());
        assertNotSame(hashtable, bean.getJndiProperties());
    }
}