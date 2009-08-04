/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.tests.simple;

import org.junit.Test;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * EnvironmentBean tests
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class EnvironmentBeanTest
{
    @Test
    public void beanPopulatorTestWithDummies() throws Exception {

        // check that a bean is populated correctly by the BeanPopulator

        DummyEnvironmentBean testBean = new DummyEnvironmentBean();
        DummyPropertyManager testManager = new DummyPropertyManager(testBean.getProperties());
        BeanPopulator.configureFromPropertyManager(testBean, testManager);
        Set<String> expectedKeys = testBean.getProperties().stringPropertyNames();

        assertTrue( testManager.usedKeys.containsAll(expectedKeys) );
    }
}
