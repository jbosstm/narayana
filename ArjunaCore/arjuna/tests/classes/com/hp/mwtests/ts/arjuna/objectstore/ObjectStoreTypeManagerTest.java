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
package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.type.ObjectStoreTypeManager;
import com.arjuna.ats.arjuna.objectstore.type.ObjectStoreTypeMap;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectStoreTypeManagerTest
{
    @Test
    public void testObjectStoreTypeManager()
    {
        Class<? extends ObjectStore> clazz = ObjectStoreType.typeToClass(ObjectStoreType.VOLATILE);
        int typeCode = ObjectStoreType.classToType(clazz);

        assertEquals(ObjectStoreType.VOLATILE, typeCode);

        ObjectStoreTypeManager manager = ObjectStoreTypeManager.manager();

        manager.add(new DummyObjectStoreTypeMap());
        
        assertTrue(manager.getObjectStoreClass(1001) != null);
        assertEquals(1001, manager.getType(DummyObjectStoreImpl.class));

        assertEquals(ObjectStoreType.typeToClass(1001), DummyObjectStoreImpl.class);
        assertEquals(1001, ObjectStoreType.classToType(DummyObjectStoreImpl.class));

        manager.add(new DummyObjectStoreTypeMap());

        assertEquals(DummyObjectStoreImpl.class, manager.getObjectStoreClass(1001));
        assertEquals(1001, manager.getType(DummyObjectStoreImpl.class));

        assertEquals(DummyObjectStoreImpl.class, ObjectStoreType.typeToClass(1001));
        assertEquals(1001, ObjectStoreType.classToType(DummyObjectStoreImpl.class));
    }

    class DummyObjectStoreTypeMap implements ObjectStoreTypeMap
    {
        @Override
        public Class<? extends ObjectStore> getObjectStoreClass()
        {
            return DummyObjectStoreImpl.class;
        }

        @Override
        public int getType()
        {
            return 1001;
        }
    }

    class DummyObjectStoreImpl extends VolatileStore{}
}
