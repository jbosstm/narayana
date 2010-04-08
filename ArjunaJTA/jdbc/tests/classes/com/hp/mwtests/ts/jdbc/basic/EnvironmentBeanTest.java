/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.hp.mwtests.ts.jdbc.basic;

import java.util.Hashtable;

import com.arjuna.ats.jdbc.common.JDBCEnvironmentBean;

import org.junit.Test;
import static org.junit.Assert.*;

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
