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
 * $Id: ObjectStoreCellRenderer.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ObjectStoreBrowserNode;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.common.Uid;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.awt.*;

public class ObjectStoreCellRenderer extends DefaultTreeCellRenderer
{
	private final static String EMPTY_CONTAINER_ICON_FILENAME = "objectstore-empty.gif";
    private final static String LIST_ICON_FILENAME = "objectstore-list.gif";

	private static ImageIcon	_emptyContainerIcon = new ImageIcon(ClassLoader.getSystemResource( EMPTY_CONTAINER_ICON_FILENAME ));
    private static ImageIcon	_listIcon = new ImageIcon(ClassLoader.getSystemResource( LIST_ICON_FILENAME ));
	/**
	 * Configures the renderer based on the passed in components.
	 * The value is set from messaging the tree with
	 * <code>convertValueToText</code>, which ultimately invokes
	 * <code>toString</code> on <code>value</code>.
	 * The foreground color is set based on the selection and the icon
	 * is set based on on leaf and expanded.
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
												  boolean sel,
												  boolean expanded,
												  boolean leaf, int row,
												  boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		/** Get tree path for this row **/
		TreePath selectedPath = tree.getPathForRow(row);

		if ( selectedPath != null )
		{
            if ( selectedPath.getLastPathComponent() instanceof ObjectStoreBrowserNode )
            {
                ObjectStoreBrowserNode node = (ObjectStoreBrowserNode)selectedPath.getLastPathComponent();
                setToolTipText( "Type:"+node.getType() );
            }

            if ( selectedPath.getLastPathComponent() instanceof ListNode )
            {
                setIcon( _listIcon );
            }
            else
            {
			    setIcon( _emptyContainerIcon );
            }
		}

		return this;
	}
}
