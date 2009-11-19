/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;

public class ExtendedResourceRecordViewer extends ResourceRecordViewer //AbstractRecordViewer
{
	public ExtendedResourceRecordViewer() throws Exception
	{
		super();
	}

    protected void updateTableData(AbstractRecord record, StatePanel statePanel)
    {
        super.updateTableData(record, statePanel);

        if (record instanceof ExtendedResourceRecord)
        {
            ExtendedResourceRecord xarr = (ExtendedResourceRecord) record;

            if (xarr.value() != null)
                statePanel.setData("Resource", xarr.value().toString());
        }
    }

    public void entrySelected(final AbstractRecord record,
                              final BasicAction action,
                              final ObjectStoreViewEntry entry,
                              final StatePanel statePanel) throws ObjectStoreException
    {
        super.entrySelected(record, action, entry, statePanel);
    }

    /**
     * Get the type this state viewer is intended to be registered against. Should match up with
     * the record store type() (shame it's not a static method
     * @return
     */
    public String getType()
    {
        return "/StateManager/AbstractRecord/ExtendedResourceRecord";
    }
}
