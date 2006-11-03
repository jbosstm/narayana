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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BrowserFrame.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.objectstorebrowser.frames;

import com.arjuna.ats.tools.objectstorebrowser.panels.*;
import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreCellRenderer;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.*;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.StateViewersRepository;

import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanel;
import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreBrowserPlugin;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.ObjectStoreRootProvider;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class BrowserFrame extends JInternalFrame implements TreeSelectionListener, TreeWillExpandListener, IconSelectionListener, ActionListener
{
	private final static String FRAME_TITLE = "Object Store Browser";

	public final static char GROUP_DELIMITER = File.separatorChar;

    private static DefaultTreeModel _treeModel = null;
    private static StatePanel _stateViewer = null;
    private static JTree _tree = null;
    private static ObjectStore _objectStore = null;

	private JSplitPane _splitPane = null;
	private IconPanel _objectView = null;
    private JComboBox _rootCombo = null;

	public BrowserFrame()
	{
		super(FRAME_TITLE, true, true, true, true);

		/** Set frames size **/
		this.setSize(500, 300);

        /** Create tree panel **/
        JPanel treePanel = new JPanel();
        treePanel.setBackground(Color.white);
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        treePanel.setLayout(gbl);

		/** Create components **/
        /** Create root pulldown **/
        _rootCombo = new JComboBox(ObjectStoreBrowserPlugin.getRootProvider().getRoots());
        _rootCombo.addActionListener(this);

        if ( _rootCombo.getItemCount() == 0 )
        {
            _rootCombo.setEnabled(false);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbl.addLayoutComponent(_rootCombo,gbc);
        treePanel.add(_rootCombo);

        ObjectStoreRootProvider provider = ObjectStoreBrowserPlugin.getRootProvider();

        /** Create object store **/
        if ( provider == null || provider.getRoots().isEmpty() )
        {
            JOptionPane.showMessageDialog(this, "No object store roots found, object store is empty", "Warning", JOptionPane.WARNING_MESSAGE);
            dispose();
        }
        else
        {
            String rootStr = (String)provider.getRoots().firstElement();

            _objectStore = new ObjectStore(new ClassName(rootStr));

            _treeModel = new DefaultTreeModel(createTree());
            _tree = new JTree(_treeModel);
            _tree.addTreeSelectionListener(this);
            _tree.addTreeWillExpandListener(this);
            _tree.setCellRenderer(new ObjectStoreCellRenderer());
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1;
            gbc.weightx = 1;
            JScrollPane jsp = new JScrollPane(_tree);
            gbl.addLayoutComponent(jsp,gbc);
            treePanel.add(jsp);

            /** Create state viewer panel **/
            _stateViewer = new StatePanel();
            _stateViewer.setBackground(Color.white);

            /** Create object view **/
            _objectView = new IconPanel();
            JScrollPane objectScrollPane = new JScrollPane(_objectView);

            /** Create right hand split panel **/
            JSplitPane rightHandSplitPlane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, objectScrollPane, new JScrollPane(_stateViewer));
            rightHandSplitPlane.setDividerSize(3);
            rightHandSplitPlane.setDividerLocation((int)(getHeight() * 0.5));

            /** Create split pane **/
            this.getContentPane().add(_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, rightHandSplitPlane));
            _splitPane.setDividerSize(3);

            show();
        }
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

    public void actionPerformed(ActionEvent e)
    {
        String commandAction = e.getActionCommand();

        if ( commandAction != null )
        {
            String objectStoreRoot = (String)_rootCombo.getSelectedItem();
            _objectStore = new ObjectStore(new com.arjuna.ats.arjuna.gandiva.ClassName(objectStoreRoot));
            _treeModel.setRoot(createTree());
        }
    }

    private DefaultMutableTreeNode createTree()
	{
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Object Store");

		try
		{
			InputObjectState types = new InputObjectState();

			if (_objectStore.allTypes(types))
			{
				String theName = null;

				try
				{
					boolean endOfList = false;

					while (!endOfList)
					{
						theName = types.unpackString();

						if (theName.compareTo("") == 0)
							endOfList = true;
						else
						{
							SubTreeNode childNode;
                            DefaultMutableTreeNode currentNode = rootNode;
							String parseName = new String(theName);

							if ( parseName.indexOf(GROUP_DELIMITER) != -1 )
							{
								while ( parseName.indexOf(GROUP_DELIMITER) != -1 )
								{
									String group = parseName.substring(0, parseName.indexOf(GROUP_DELIMITER) );

                                    parseName = parseName.substring( parseName.indexOf(GROUP_DELIMITER) + 1 );
									currentNode = getChildWithName(currentNode, group);
								}

								childNode = new ObjectStoreDirectoryNode(parseName, parseName);
                                childNode.setIconPanelEntry(new ObjectStoreViewEntry(parseName, parseName, childNode));
								currentNode.add(childNode);
                                childNode.add(new DefaultMutableTreeNode(""));
							}
							else
							{
   								childNode = new ObjectStoreDirectoryNode(theName, theName);
                                childNode.setIconPanelEntry(new ObjectStoreViewEntry(theName, theName, childNode));
                                currentNode.add(childNode);
                                childNode.add(new DefaultMutableTreeNode(""));
							}
						}
					}
				}
				catch (Exception e)
				{
					// end of list!
                    e.printStackTrace(System.err);
				}
			}
		}
		catch (Exception e)
		{
            JOptionPane.showMessageDialog(this, "An error occurred while creating the object browser tree");
			System.err.println("Caught unexpected exception: " + e);
            e.printStackTrace(System.err);
		}

		return rootNode;
	}

	/**
	 * Invoked whenever a node in the tree is about to be expanded.
	 */
	public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
	{
		/** Find the path that has been expanded then ensure the children nodes are up-to-date **/
		TreePath expandedPath = e.getPath();

        updateTreePath(expandedPath);
	}

    /**
     * Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    public void valueChanged(TreeSelectionEvent e)
    {
        TreePath selectedPath = e.getPath();
        String theName = "";

        for (int count=1;count<selectedPath.getPathCount();count++)
        {
            theName += ((String)((DefaultMutableTreeNode)selectedPath.getPathComponent(count)).getUserObject()) + GROUP_DELIMITER;
        }

        try
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();

            updateTreePath(selectedPath);
            setupObjectViewPanel(theName, node);

            _objectView.layoutContainer();
            validate();
            repaint();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

	/**
	 * Invoked whenever a node in the tree is about to be collapsed.
	 */
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
	{
	}

	private void updateTreePath(TreePath path)
	{
		String pathPrefix = "";
        DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        // Skip the root node as it is not part of the underlying object store tree
        for (int count=1;count<path.getPathCount();count++)
        {
            pathPrefix += (String)((DefaultMutableTreeNode)path.getPathComponent(count)).getUserObject() + GROUP_DELIMITER;
        }

        updateTreeNode(lastNode, pathPrefix);

        if ( lastNode instanceof ListNode )
        {
            if ( ((ListNode)lastNode).getListNodeListener() != null )
            {
                ((ListNode)lastNode).getListNodeListener().listExpanded((ListNode)lastNode);
            }
        }
        else
        if ( lastNode instanceof ListEntryNode )
        {
            if ( ((ListEntryNode)lastNode).getListEntryNodeListener() != null )
            {
                ((ListEntryNode)lastNode).getListEntryNodeListener().listEntryNodeExpanded((ListEntryNode)lastNode);
            }
        }
        else
        if ( lastNode instanceof UidNode )
        {
            try
            {
                StateViewersRepository.lookupStateViewer(((UidNode)lastNode).getType()).uidNodeExpanded(_objectStore, ((UidNode)lastNode).getType(), (UidNode)lastNode, (UidNode)lastNode, _stateViewer);
            }
            catch (ObjectStoreException e)
            {
                JOptionPane.showMessageDialog(this, "An error occurred while creating the state viewer");
                e.printStackTrace(System.err);
            }
        }
    }

    private void updateTreeNode(DefaultMutableTreeNode node, String pathPrefix)
    {
        ArrayList nodesAdded = new ArrayList();
        String theName = null;

        if ( node instanceof ObjectStoreDirectoryNode || node == _treeModel.getRoot() )
        {
            try
            {
                InputObjectState types = new InputObjectState();

                if (_objectStore.allTypes(types))
                {
                    try
                    {
                        boolean endOfList = false;

                        while (!endOfList)
                        {
                            theName = types.unpackString();

                            if (theName.compareTo("") == 0)
                                endOfList = true;
                            else
                            {
                                /** If the object is in the folder we are looking at then make sure it exists in the tree **/
                                if ( theName.startsWith(pathPrefix) )
                                {
                                    String newChild = theName.substring(pathPrefix.length());

                                    // Ensure this node doesn't contain more nodes
                                    if ( newChild.indexOf(GROUP_DELIMITER) == -1 )
                                    {
                                        SubTreeNode newNode;

                                        if ( getChildWithName(node, newChild) == null )
                                        {
                                            _treeModel.insertNodeInto( newNode = new ObjectStoreDirectoryNode(newChild, newChild), node, node.getChildCount() );
                                            newNode.setIconPanelEntry(new ObjectStoreViewEntry(theName, theName, newNode));
                                            _treeModel.insertNodeInto( new ObjectStoreDirectoryNode(this, pathPrefix), newNode, newNode.getChildCount() );
                                        }
                                        nodesAdded.add(newChild);
                                    }
                                    else
                                    {
                                        DefaultMutableTreeNode newNode;

                                        String name = newChild.substring(0, newChild.indexOf(GROUP_DELIMITER));

                                        if ( getChildWithName(node, name) == null )
                                        {
                                            _treeModel.insertNodeInto( newNode = new ObjectStoreDirectoryNode(name, name), node, node.getChildCount() );
                                            ((ObjectStoreBrowserNode)newNode).setIconPanelEntry(new ObjectStoreViewEntry(theName, theName, (ObjectStoreBrowserNode)newNode));
                                            _treeModel.insertNodeInto( new ObjectStoreDirectoryNode(this, pathPrefix), newNode, newNode.getChildCount() );
                                        }
                                        nodesAdded.add(name);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        // End of list
                    }
                }

                theName = File.separator + pathPrefix;

                if ( theName.length() > 0 )
                {
                    InputObjectState uids = new InputObjectState();

                    if (_objectStore.allObjUids(theName, uids))
                    {
                        Uid theUid = new Uid();

                        try
                        {
                            boolean endOfUids = false;

                            while (!endOfUids)
                            {
                                theUid.unpack(uids);

                                if (theUid.equals(Uid.nullUid()))
                                    endOfUids = true;
                                else
                                {
                                    UidNode uidNode;

                                    if ( getChildWithName(node, theName) == null )
                                    {
                                        _treeModel.insertNodeInto(uidNode = new UidNode(theUid.toString(), theUid, theName), node, _treeModel.getChildCount(node));
                                        uidNode.setIconPanelEntry(new ObjectViewEntry(theName, theUid.toString(), _objectStore.currentState(theUid, theName), uidNode));
                                        _treeModel.insertNodeInto(new DefaultMutableTreeNode(""), uidNode, _treeModel.getChildCount(uidNode));
                                    }
                                    nodesAdded.add(theUid.toString());
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // Ignore
            }

            for (int count=0;count<_treeModel.getChildCount(node);count++)
            {
                DefaultMutableTreeNode checkNode = (DefaultMutableTreeNode)_treeModel.getChild(node, count);
                if ( !nodesAdded.contains(checkNode.getUserObject().toString()) )
                {
                    _treeModel.removeNodeFromParent(checkNode);
                }
                else
                {
                    nodesAdded.remove(checkNode.getUserObject().toString());
                }
            }
        }
	}


	private void setupObjectViewPanel(String theName, DefaultMutableTreeNode parentNode)
	{
		if ( theName.length() > 0 )
		{
            /** Reset the icons **/
			_objectView.resetIcons();

            /** Add the icons for each node in the tree **/
            for (int count=0;count<_treeModel.getChildCount(parentNode);count++)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)_treeModel.getChild(parentNode, count);

                if ( node instanceof ObjectStoreBrowserNode )
                {
                    ObjectStoreBrowserNode browserNode = (ObjectStoreBrowserNode)node;
                    IconPanelEntry entry = browserNode.getIconPanelEntry();

                    if ( entry != null )
                    {
                        _objectView.addIcon(entry);
                        entry.addSelectionListener(this);
                    }
                }
            }
		}
	}

	public void iconSelected(IconPanelEntry icon, boolean selected)
	{
		try
		{
			if ( _objectView.getSelectedEntry() == null )
			{
				_stateViewer.removeAll();
				validate();
				repaint();
			}

			if ( selected )
			{
				// Do nothing
			}
		}
		catch (Exception e)
		{
            e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(this, "An error occurred while creating the state viewer");
		}
	}

    public static ObjectStore getObjectStore()
    {
        return _objectStore;
    }

    public static StatePanel getStatePanel()
    {
        return _stateViewer;
    }

    public static DefaultTreeModel getTreeModel()
    {
        return _treeModel;
    }

    public static JTree getTree()
    {
        return _tree;
    }
}
