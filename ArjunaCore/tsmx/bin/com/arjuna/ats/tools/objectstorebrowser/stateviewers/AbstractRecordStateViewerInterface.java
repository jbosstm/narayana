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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AbstractRecordStateViewerInterface.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;

/**
 * This interface is implemented by classes which display the
 * state of abstract records in the object store.  Each state viewer
 * can only display the state of a single object type.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $id:$
 */
public interface AbstractRecordStateViewerInterface
{
    /**
     * An entry has been selected of the type this viewer is registered against.
     *
     * @param record
     * @param action
     * @param entry
     * @param statePanel
     * @throws com.arjuna.ats.arjuna.exceptions.ObjectStoreException
     */
    public void entrySelected(AbstractRecord record,
                              BasicAction action,
                              ObjectStoreViewEntry entry,
                              StatePanel statePanel) throws ObjectStoreException;

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType();
}
