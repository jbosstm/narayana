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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.arjunatransaction.nodes;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FailedListNode.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNodeListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.arjuna.coordinator.RecordList;

public class FailedListNode extends ArjunaTransactionListNode implements ListNodeListener, IconSelectionListener
{
    public FailedListNode(Object userObject, Object assObject, String type)
    {
        super(userObject, assObject, type);
    }

    public RecordList getList()
    {
        return getAction().getFailedList();
    }
}
