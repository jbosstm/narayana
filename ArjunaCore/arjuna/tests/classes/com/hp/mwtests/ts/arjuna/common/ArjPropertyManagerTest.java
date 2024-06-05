/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.common;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArjPropertyManagerTest {
    @Test
    public void test() {
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreSync(false);
        assertFalse(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreSync(false);
        assertFalse(arjPropertyManager.getObjectStoreEnvironmentBean().isObjectStoreSync());

        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreSync(true);
        assertTrue(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());
        assertFalse(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).isObjectStoreSync());
        assertFalse(arjPropertyManager.getObjectStoreEnvironmentBean().isObjectStoreSync());

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreSync(true);
        assertTrue(arjPropertyManager.getObjectStoreEnvironmentBean().isObjectStoreSync());
        assertTrue(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());


        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreSync(false);
        assertFalse(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).isObjectStoreSync());
        assertFalse(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());

        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreSync(false);
        assertFalse(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).isObjectStoreSync());
        assertFalse(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());

        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreSync(true);
        assertTrue(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).isObjectStoreSync());
        assertTrue(BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").isObjectStoreSync());
    }

    @Test
    public void testNodeIdentifier() throws CoreEnvironmentBeanException {
        CoreEnvironmentBean config = BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class);
        String random = UUID.randomUUID().toString(); // String form of a random UUID

        assertEquals(36, random.length()); // a UUID consists of 32 hex digits along with 4 “-” symbols

        config.setNodeIdentifier(random);//random.getBytes());
        byte[] bytes = config.getNodeIdentifierBytes();
        String name1 = config.getNodeIdentifier();
        String name2 = new String(bytes);

        assertEquals(name1, name2);
        assertEquals(name1, random);
    }
}
