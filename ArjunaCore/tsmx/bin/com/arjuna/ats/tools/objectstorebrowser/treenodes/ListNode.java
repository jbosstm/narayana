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
package com.arjuna.ats.tools.objectstorebrowser.treenodes;

import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ListNode.java 2342 2006-03-30 13:06:17Z  $
 */


public class ListNode extends SubTreeNode implements ListNodeListener
{
    private ListNodeListener _listener = null;
    private Object           _assObject = null;

    public ListNode(Object userObject, Object assObject, String type)
    {
        super(userObject, type);

        _assObject = assObject;
        _listener = this;
    }

    public void registerListNodeListener(ListNodeListener listener)
    {
        _listener = listener;
    }

    public void listExpanded(ListNode node)
    {
        DefaultTreeModel tree = BrowserFrame.getTreeModel();

        while ( tree.getChildCount(node) > 0 )
        {
            tree.removeNodeFromParent((DefaultMutableTreeNode)tree.getChild(node, 0));
        }
    }

    public ListNodeListener getListNodeListener()
    {
        return _listener;
    }

    public Object getAssObject()
    {
        return _assObject;
    }
}
