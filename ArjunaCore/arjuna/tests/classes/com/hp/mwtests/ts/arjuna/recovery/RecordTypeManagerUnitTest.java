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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CallbackRecoveryTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.recovery;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;

import static org.junit.Assert.*;


class DummyMap implements RecordTypeMap
{

    public Class<? extends AbstractRecord> getRecordClass ()
    {
        return PersistenceRecord.class;
    }

    public int getType ()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
public class RecordTypeManagerUnitTest
{
    @Test
    public void test()
    {
        DummyMap map = new DummyMap();
        RecordTypeManager.manager().add(map);
        
        assertEquals(RecordTypeManager.manager().getClass(0), PersistenceRecord.class);
        assertEquals(RecordTypeManager.manager().getType(PersistenceRecord.class), 0);
        
        RecordTypeManager.manager().remove(map);
    }
}
