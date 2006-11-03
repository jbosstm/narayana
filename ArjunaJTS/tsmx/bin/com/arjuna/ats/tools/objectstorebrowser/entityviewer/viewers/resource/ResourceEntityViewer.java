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
package com.arjuna.ats.tools.objectstorebrowser.entityviewer.viewers.resource;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceEntityViewer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.entityviewer.EntityViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.entityviewer.viewers.resource.ResourceViewDialog;

import javax.swing.*;

/**
 * Entity viewer for a org.omg.CosTransactions.Resource
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: ResourceEntityViewer.java 2342 2006-03-30 13:06:17Z  $
 */
public class ResourceEntityViewer implements EntityViewerInterface
{
    /**
     * This method is called when an entity has been selected to be viewed whose type has been registered
     * against this viewer.
     * @param type The type of the abstract record which was selected.
     * @param value The value of the abstract record which was selected.
     * @param panel The panel to display the type information within
     */
    public void viewEntity(String type, Object value, JPanel panel)
    {
        if ( value instanceof ResourceActionHandle )
        {
            ResourceActionHandle rah = (ResourceActionHandle)value;

            new ResourceViewDialog(null, type, rah);
        }
    }
}
