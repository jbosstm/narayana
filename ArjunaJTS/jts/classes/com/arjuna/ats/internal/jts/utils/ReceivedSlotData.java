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
package com.arjuna.ats.internal.jts.utils;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
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
public class ReceivedSlotData {
    org.omg.PortableInterceptor.Current _piCurrent;
    Any slotData;

    /**
     * save the received data slot
     */
    public ReceivedSlotData() {
        try {
            if (_piCurrent == null)
                _piCurrent = org.omg.PortableInterceptor.CurrentHelper.narrow(
                        ORBManager.getORB().orb().resolve_initial_references("PICurrent"));

            if (_piCurrent != null)
                slotData = _piCurrent.get_slot(OTSManager.getReceivedSlotId());

        } catch (InvalidName invalidName) {
        } catch (InvalidSlot invalidSlot) {
        }
    }

    /**
     * restore the received data slot
     */
    public void restoreSlot() {
        if (slotData != null && _piCurrent != null && (slotData.type().kind().value() != TCKind._tk_null)) {
            try {
                _piCurrent.set_slot(OTSManager.getReceivedSlotId(), slotData);
            } catch (InvalidSlot invalidSlot) {
            }
        }
    }
}
