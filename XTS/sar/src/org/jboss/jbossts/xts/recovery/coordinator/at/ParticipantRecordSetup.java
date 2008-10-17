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
package org.jboss.jbossts.xts.recovery.coordinator.at;

import com.arjuna.ats.arjuna.gandiva.inventory.InventoryElement;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.coordinator.RecordType;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord;

/**
 * Created by IntelliJ IDEA.
 * User: jhalli
 * Date: Aug 20, 2007
 * Time: 3:18:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParticipantRecordSetup implements InventoryElement {

    public synchronized Object createVoid ()
    {
        return ParticipantRecord.create();
    }

    public synchronized Object createClassName (ClassName className)
    {
        return null;
    }

    public synchronized Object createObjectName (ObjectName objectName)
    {
        return null;
    }

    public synchronized Object createResources (Object[] resources)
    {
        return null;
    }

    public synchronized Object createClassNameResources (ClassName className, Object[] resources)
    {
        return null;
    }

    public synchronized Object createObjectNameResources (ObjectName objectName, Object[] resources)
    {
        return null;
    }

    public ClassName className ()
    {
        return RecordType.typeToClassName(RecordType.USER_DEF_FIRST0);
                //ClassName("WSATParticipantRecord"); // TODO remove dupl with ParticipantRecord
    }

}
