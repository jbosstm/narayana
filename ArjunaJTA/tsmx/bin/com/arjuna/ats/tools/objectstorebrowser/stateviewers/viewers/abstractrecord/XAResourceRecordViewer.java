/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord;

import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.XAResourceInfo;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

import javax.transaction.xa.XAResource;

public class XAResourceRecordViewer extends AbstractRecordViewer
{
    protected void updateTableData(AbstractRecord record, StatePanel statePanel)
    {
        super.updateTableData(record, statePanel);

        if (record instanceof XAResourceRecord)
        {
            XAResourceRecord xarr = (XAResourceRecord) record;

            if (xarr.value() != null)
            {
                try
                {
                    XAResourceInfo xares = new XAResourceInfo((XAResource) xarr.value(), xarr.getXid(), "UNKNOWN");

                    statePanel.setData("Product Name", xares.getEisProductName());
                    statePanel.setData("Product Version", xares.getEisProductVersion());
                    statePanel.setData("Tx State", xares.getTxState());
                    statePanel.setData("Xid", xares.getXid());
                    statePanel.setData("Timeout", String.valueOf(xares.getTimeout()));
                }
                catch (Exception e)
                {
                    statePanel.setData("XA Resource Info", xarr.value().toString());
                }
            }
        }
    }

    /**
     * Get the type this state viewer is intended to be registered against. Should match up with
     * the record store typeName()
     * @return
     */
    public String getType()
    {
        return XAResourceRecord.typeName(); //"/StateManager/AbstractRecord/XAResourceRecord";
    }
}