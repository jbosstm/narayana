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
package com.arjuna.ats.tools.objectstorebrowser.treenodes;

import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreBrowserTreeManipulationInterface;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;

import javax.swing.tree.DefaultMutableTreeNode;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SubTreeNode.java 2342 2006-03-30 13:06:17Z  $
 */

public class SubTreeNode extends ObjectStoreBrowserNode implements ObjectStoreBrowserTreeManipulationInterface
{
    public SubTreeNode(Object userObject, String type)
    {
        super(userObject, type);
    }

    public SubTreeNode(Object userObject, boolean allowsChildren)
    {
        super(userObject, allowsChildren);
    }

    private final static DefaultMutableTreeNode getChildWithName(DefaultMutableTreeNode currentNode, String name)
    {
        for (int count=0;count<currentNode.getChildCount();count++)
        {
            String currentName = (String)((DefaultMutableTreeNode)currentNode.getChildAt(count)).getUserObject();

            if ( name.equals(currentName) )
            {
                return (DefaultMutableTreeNode)currentNode.getChildAt(count);
            }
        }

        return null;
    }

    public ObjectStoreBrowserTreeManipulationInterface getEntrySubTree(String name)
    {
        DefaultMutableTreeNode childNode = getChildWithName(this, name);

        return childNode instanceof SubTreeNode ? (ObjectStoreBrowserTreeManipulationInterface)childNode : null;
    }

    public IconPanelEntry getEntry(String name)
    {
        return ((ObjectStoreBrowserNode)getChildWithName(this, name)).getIconPanelEntry();
    }

    public boolean isSubTree(String name)
    {
        return getChildWithName(this,name) instanceof SubTreeNode;
    }

    public void createEntry(ObjectStoreBrowserNode node)
    {
        BrowserFrame.getTreeModel().insertNodeInto(node, this, BrowserFrame.getTreeModel().getChildCount(this));
    }

    public ObjectStoreBrowserTreeManipulationInterface createEntrySubTree(SubTreeNode node)
    {
        createEntry(node);

        return node;
    }

    public void clearEntries()
    {
        while ( BrowserFrame.getTreeModel().getChildCount(this) != 0 )
        {
            BrowserFrame.getTreeModel().removeNodeFromParent((DefaultMutableTreeNode)BrowserFrame.getTreeModel().getChild(this, 0));
        }
    }
}
