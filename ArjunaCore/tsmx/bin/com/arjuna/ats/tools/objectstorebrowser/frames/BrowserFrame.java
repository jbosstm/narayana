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
import com.arjuna.ats.tools.objectstorebrowser.treenodes.IUidCollection;

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
import com.arjuna.ats.internal.arjuna.common.UidHelper;

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
    private JLabel    _statusBar = new JLabel();

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
        _rootCombo.setEnabled(_rootCombo.getItemCount() != 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbl.addLayoutComponent(_rootCombo,gbc);
        treePanel.add(_rootCombo);

        JComponent sp = getSearchPanel();
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0;
        gbl.addLayoutComponent(sp,gbc);
        treePanel.add(sp);

        ObjectStoreRootProvider provider = ObjectStoreBrowserPlugin.getRootProvider();

        /** Create object store **/
        if ( provider == null || provider.getRoots().isEmpty() )
        {
            JOptionPane.showMessageDialog(this, "No object store roots found, object store is empty", "Error", JOptionPane.WARNING_MESSAGE);
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
            gbc.gridy = 2;
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

            // and finally a status bar
            _statusBar.setBorder(new javax.swing.border.EtchedBorder());
            this.getContentPane().add(_statusBar, BorderLayout.SOUTH);
            _stateViewer.setStatusBar(_statusBar);

            show();
        }
	}

    /**
     * Create an edit box for automatically navigating to a particular tree node
     *
     * @return a component containing the search box and its label
     */
    private JComponent getSearchPanel()
    {
        Box box = new Box(BoxLayout.X_AXIS);

        box.add(new JLabel("Uid: "));
        JTextField searchBox = new JTextField(20);

        searchBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                // locate a node starting from the currently selected node
                TreePath currentSelection = getSelectedPath();
                DefaultMutableTreeNode node = locateNode(currentSelection, e.getActionCommand());

                if (node != null)
                {
                    TreePath path = new TreePath(node.getPath());
                    
                    _tree.expandPath(path);
                    _tree.setSelectionPath(path);
                    _tree.scrollPathToVisible(path);
                    expandTree(_tree, node);
                }
            }
        });
        box.add(searchBox);
        box.setToolTipText("Enter a uid to search for");

        return box;
    }

    /**
     * Return the currently selected node. If none is selected then
     * make the root node the currently selected node.
     *
     * @return the current selected node
     */
    private TreePath getSelectedPath()
    {
        TreePath currentSelection = _tree.getSelectionPath();

        if (currentSelection == null)
        {
            TreePath path = new TreePath(_treeModel.getRoot());

            _tree.setSelectionPath(path);
            return path;
        }

        return currentSelection;
    }

    private void expandTree(JTree tree, DefaultMutableTreeNode start)
    {
        for (Enumeration e = start.children(); e.hasMoreElements();)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();

            if (!node.isLeaf())
            {
                tree.expandPath(new TreePath(node.getPath()));
                expandTree(tree, node);
            }
        }
    }

    /**
     * Convert a tree path to an object store path
     *
     * @param path the tree path
     * @return the corresponding object store path
     */
    private String getPathName(TreePath path)
    {
		String pathName = "";

        // Skip the root node as it is not part of the underlying object store tree
        for (int i = 1; i < path.getPathCount(); i++)
            pathName += ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject().toString() + GROUP_DELIMITER;

        return pathName;
    }

    /**
     * Locate a node and simultaneously update each node in the path to the target node.
     *
     * @param path where to start the search from
     * @param name the name of the node to look for
     * @return the first node with the given name or null if not found under path
     */
    private DefaultMutableTreeNode locateNode(TreePath path, String name)
    {
        DefaultMutableTreeNode node = findNode((DefaultMutableTreeNode) path.getLastPathComponent(), name);

        if (node != null)
            return node;

        // not found so try updating the tree and then search again
		String pathName = getPathName(path);

        node = (DefaultMutableTreeNode)path.getLastPathComponent();
        updateTreeNode(node, pathName);

        // if the node contains a collection of ids ...
        if (node instanceof IUidCollection && ((IUidCollection) node).contains(name))
            return node;

        for (Enumeration e = node.children(); e.hasMoreElements();)
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
            
            path = new TreePath(child.getPath());
            node = locateNode(path, name);

            if (node != null)
                return node;
        }

        return null;    // not found
    }

    /**
     * Locate a node by name
     *
     * @param node the node from which to start the search
     * @param name the name of the node to search for
     * @return null if no matching node can be found
     */
    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, String name)
    {
        if (node.getUserObject().toString().equals(name))
            return node;

        for (int i = 0; i < node.getChildCount(); i++)
        {
            DefaultMutableTreeNode child = findNode((DefaultMutableTreeNode) node.getChildAt(i), name);

            if (child != null)
                return child;
        }

        return null;
    }

    private static DefaultMutableTreeNode getChildWithName(DefaultMutableTreeNode currentNode, String name)
	{
        for (int count=0;count<currentNode.getChildCount();count++)
		{
            String currentName = ((DefaultMutableTreeNode)currentNode.getChildAt(count)).getUserObject().toString();

			if ( name.equals(currentName) )
			{
				return (DefaultMutableTreeNode)currentNode.getChildAt(count);
			}
		}

		return null;
	}

    /**
     * Triggered when the object store root combo is changed
     *
     * @param e the triggering action containing the name of the target store root
     */
    public void actionPerformed(ActionEvent e)
    {
        String commandAction = e.getActionCommand();

        if ( commandAction != null )
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String objectStoreRoot = (String)_rootCombo.getSelectedItem();

            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            _stateViewer.clear();
            _stateViewer.setVisible(false);
            _objectStore = new ObjectStore(new com.arjuna.ats.arjuna.gandiva.ClassName(objectStoreRoot));
            _treeModel.setRoot(createTree());

            Thread.currentThread().setContextClassLoader(loader);

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
				String theName;

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
							String parseName = theName;

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
            _stateViewer.reportError("An error occurred while creating the object browser tree");
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
        String theName = getPathName(selectedPath);

        _stateViewer.clearStatus();

        if (e.getNewLeadSelectionPath() == null)
            return; // the whole tree is going to change so bail out early

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
		String pathPrefix = getPathName(path);
        DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode)path.getLastPathComponent();

        updateTreeNode(lastNode, pathPrefix);

        _stateViewer.setVisible(false);
        
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
                _stateViewer.reportError("An error occurred while creating the state viewer");
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
                        Uid theUid = null;

                        try
                        {
                            boolean endOfUids = false;

                            while (!endOfUids)
                            {
                                theUid = UidHelper.unpackFrom(uids);

                                if (theUid.equals(Uid.nullUid()))
                                    endOfUids = true;
                                else
                                {
                                    UidNode uidNode;

                                    if ( getChildWithName(node, theUid.stringForm()) == null )
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

    /**
     * Initialize the object view panel in response to the user selected a new tree node
     * each view panel entry should have already been initialized during the call to
     * updateTreePath (provided the clicked node is of type UidNode and there is a viewer
     * registered against the object store type - NB viewers are defined in the jar manifest
     * for the tool).
     *
     * @param theName the object store path name (aka type) of the selected node
     * @param parentNode the selected tree node
     */
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
                _stateViewer.setVisible(false);
//                _stateViewer.removeAll();
//				validate();
//				repaint();
			}

			if ( selected )
			{
                _stateViewer.setVisible(true);
                // Do nothing
			}
		}
		catch (Exception e)
		{
            e.printStackTrace(System.err);
			_stateViewer.reportError("An error occurred while creating the state viewer");
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
