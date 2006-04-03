/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.objectstorebrowser.stateviewers.StateViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.UidNode;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DefaultStateViewer.java 2342 2006-03-30 13:06:17Z  $
 */

public class DefaultStateViewer implements StateViewerInterface
{
    /**
     * A uid node of the type this viewer is registered against has been expanded.
     * @param os
     * @param type
     * @param manipulator
     * @param node
     * @throws ObjectStoreException
     */
    public void uidNodeExpanded(ObjectStore os,
                                String type,
                                ObjectStoreBrowserTreeManipulationInterface manipulator,
                                UidNode node,
                                StatePanel infoPanel) throws ObjectStoreException
    {
    }

    /**
     * An entry has been selected of the type this viewer is registered against.
     *
     * @param os
     * @param type
     * @param uid
     * @param entry
     * @param statePanel
     * @throws ObjectStoreException
     */
    public void entrySelected(ObjectStore os,
                              String type,
                              Uid uid,
                              ObjectStoreViewEntry entry,
                              StatePanel statePanel) throws ObjectStoreException
    {
    }

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType()
    {
        return null;
    }
}
