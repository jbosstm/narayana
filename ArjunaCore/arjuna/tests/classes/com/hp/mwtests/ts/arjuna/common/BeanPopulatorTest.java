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
package com.hp.mwtests.ts.arjuna.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.tests.simple.DummyPropertyManager;
import com.arjuna.ats.arjuna.common.*;

import java.util.Set;
import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Check behaviour of the BeanPopulator util which copies old PropertyManager values into new EnvironmentBeans.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class BeanPopulatorTest
{
    @Test
    public void testArjunaCorePropertiesPopulation() throws Exception {

        // check that all the Environment properties are looked for
        // by the set of beans which wrap them and conversely that no undefined
        // properties are looked for. i.e. that the Environment and Beans are in sync

        DummyPropertyManager testManager = new DummyPropertyManager(null);

        BeanPopulator.configureFromPropertyManager(new CoordinatorEnvironmentBean(), testManager);
        BeanPopulator.configureFromPropertyManager(new RecoveryEnvironmentBean(), testManager);
        BeanPopulator.configureFromPropertyManager(new CoreEnvironmentBean(), testManager);
        BeanPopulator.configureFromPropertyManager(new ObjectStoreEnvironmentBean(), testManager);

        // we expect this one to be missing - that's ok.
        testManager.usedKeys.add("com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple");

        Set<String> expectedKeys = new HashSet<String>();
        expectedKeys.addAll( DummyPropertyManager.extractKeys(Environment.class));

        assertTrue( testManager.usedKeys.containsAll(expectedKeys) );
    }
}
