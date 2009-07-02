/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AllObjUidsTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class AllObjUidsTest
{
    @Test
    public void test() throws IOException, ObjectStoreException
    {
        ObjectStore objStore = TxControl.getStore();
        String type = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/DummyAtomicAction";

        InputObjectState ios = new InputObjectState();
        objStore.allObjUids(type, ios, ObjectStore.OS_UNKNOWN);
        Uid uid = new Uid();
        uid.unpack(ios);
        assertEquals(Uid.nullUid(), uid);

        ios = new InputObjectState();
        objStore.allObjUids(type, ios, ObjectStore.OS_COMMITTED);
        uid = new Uid();
        uid.unpack(ios);
        assertEquals(Uid.nullUid(), uid);

        ios = new InputObjectState();
        objStore.allObjUids(type, ios, ObjectStore.OS_UNCOMMITTED);
        uid = new Uid();
        uid.unpack(ios);
        assertEquals(Uid.nullUid(), uid);
    }
}
