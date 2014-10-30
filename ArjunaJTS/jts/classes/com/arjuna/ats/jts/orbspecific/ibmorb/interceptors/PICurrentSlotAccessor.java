/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.jts.orbspecific.ibmorb.interceptors;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.PICurrentSlotAccessorInterface;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TCKind;
import org.omg.PortableInterceptor.InvalidSlot;

/**
 * IBM orb clears PICurrent slots on certain calls (see usages of this class for specific details)
 * - this class is provided for saving and restoring the received data slot.
 *
 * NB if there str ever issued with other slots then rename this class and add save/restore methods
 * that take a slot id as a parameter
 */
public class PICurrentSlotAccessor implements PICurrentSlotAccessorInterface {

    @Override
    public Any getData() {
        return getData(OTSManager.getReceivedSlotId());
    }

    @Override
    public void putData(Any any) {
        putData(OTSManager.getReceivedSlotId(), any);
    }

    @Override
    public Any getData(int slotId) {
        org.omg.PortableInterceptor.Current piCurrent = getPICCurrent();

        if (piCurrent != null)
            try {
                return  piCurrent.get_slot(slotId);
            } catch (InvalidSlot invalidSlot) {
            }

        return null;
    }

    @Override
    public void putData(int slotId, Any any) {
        if (any != null && any.type().kind().value() != TCKind._tk_null) {
            org.omg.PortableInterceptor.Current piCurrent = getPICCurrent();

            if (piCurrent != null)
                try {
                    piCurrent.set_slot(slotId, any);
                } catch (InvalidSlot invalidSlot) {
                }
        }
    }

    org.omg.PortableInterceptor.Current getPICCurrent() {
        try {
            return org.omg.PortableInterceptor.CurrentHelper.narrow(
                    ORBManager.getORB().orb().resolve_initial_references("PICurrent"));
        } catch (InvalidName invalidName) {
            return null;
        }
    }
}
