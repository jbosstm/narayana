/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceRecordViewer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.stateviewers.AbstractRecordStateViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.entityviewer.EntityViewerRepository;
import com.arjuna.ats.tools.objectstorebrowser.entityviewer.EntityViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.entityviewer.viewers.resource.ResourceActionHandle;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.DetailsButtonAdapter;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecoveryAbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;

import javax.swing.*;

/**
 * This is a state viewer for a JTS ResourceRecord.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: ResourceRecordViewer.java 2342 2006-03-30 13:06:17Z  $
 */
public class ResourceRecordViewer implements AbstractRecordStateViewerInterface
{
    private final static String ORB_NAME = "tools-orb";

    private ORB _orb = null;

    /**
     * When this viewer is created we need to ensure that the ORB is initialised.
     */
    public ResourceRecordViewer()
    {
        try
        {
            _orb = ORB.getInstance(ORB_NAME);
            OA oa = OA.getRootOA(_orb);

            _orb.initORB((String[])null, null);
            oa.initPOA(null);

            Implementations.initialise();
        }
        catch (Exception e)
        {
            /** The ORB has failed to initialise don't allow this plugin to be used **/
            _orb = null;
        }
    }

    /**
     * An entry has been selected of the type this viewer is registered against.
     *
     * @param record
     * @param action
     * @param entry
     * @param statePanel
     * @throws ObjectStoreException
     */
    public void entrySelected(final AbstractRecord record,
                              final BasicAction action,
                              final ObjectStoreViewEntry entry,
                              final StatePanel statePanel) throws ObjectStoreException
    {
        final RecoveryAbstractRecord rec = ((RecoveryAbstractRecord)record);

        statePanel.enableDetailsButton(new DetailsButtonAdapter() {
            public void detailsButtonPressed()
            {
                EntityViewerInterface evi = EntityViewerRepository.getEntityViewer(ResourceActionHandle.class.getName());

                if ( evi != null )
                {
                    evi.viewEntity(ResourceActionHandle.class.getName(), new ResourceActionHandle(action, rec), statePanel);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "No viewer defined for this entity");
                }
            }
        });
    }

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType()
    {
        return "/StateManager/AbstractRecord/ResourceRecord";
    }
}
