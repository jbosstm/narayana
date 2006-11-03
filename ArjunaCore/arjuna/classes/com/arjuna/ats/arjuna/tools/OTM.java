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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: OTM.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.tools;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.state.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Enumeration;
import java.net.InetAddress;

import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.NumberFormatException;

/*
 * Currently only looks at this machine.
 */

class DirEntry
{

public DirEntry ()
    {
	node = null;
	fullPathName = null;
	present = false;
    }

public DirEntry (DefaultMutableTreeNode n, String s)
    {
	node = n;
	fullPathName = s;
	present = true;
    }
    
public DefaultMutableTreeNode node;
public String fullPathName;
public boolean present;
    
};

class MonitorThread extends Thread
{

public MonitorThread (OTM arg, long timeout)
    {
	otm = arg;
	sleepTime = timeout;
    }
    
public void run ()
    {
	for (;;)
	{
	    try
	    {
		Thread.sleep(sleepTime);
	    }
	    catch (Exception e)
	    {
	    }

	    otm.updateTransactions();
	}
    }

private OTM otm;
private long sleepTime;
    
};


public class OTM extends JSplitPane
{

public OTM ()
    {
        super(HORIZONTAL_SPLIT);

	String localHost = null;
	
	try
	{
	    InetAddress myAddress = InetAddress.getLocalHost();
	    localHost = myAddress.getHostName();
	}
	catch (UnknownHostException e)
	{
	    localHost = "LocalHostUnknown";
	}

	_machines.addElement(localHost);
	localHost = null;
	
        // Create the nodes.
	
        topMachine = new DefaultMutableTreeNode("Registered machines.");
	topTran = new DefaultMutableTreeNode("Running transactions.");

	createNodes(topMachine);
	
        // Create a tree that allows one selection at a time.
	
        tree = new JTree(topMachine);
	transactions = new JTree(topTran);
	
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for when the selection changes.

	MouseListener ml = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
		 int selRow = tree.getRowForLocation(e.getX(), e.getY());
		 TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

		 if (selRow != -1)
		 {
		     if (e.getClickCount() == 2)
		     {
			 if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
			 {
			     DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getPathComponent(selRow);

			     if (node.isLeaf())
			     {
				 getTransactions(node);
			     }
			     else
				 removeTransactions();  // modifyTransactionView();
			 }
		     }
		     if (e.getClickCount() == 1)
		     {
			 if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
			     removeTransactions();
		     }
		 }
	     }
	};

	tree.addMouseListener(ml);
 
        // Create the scroll pane and add the tree to it.
	
        JScrollPane treeView = new JScrollPane(tree);
	JScrollPane tranView = new JScrollPane(transactions);
	    
        // Add the scroll panes to this panel.
	
        add(treeView);
	add(tranView);

	Dimension minimumSize = new Dimension(100, 100);

        treeView.setMinimumSize(minimumSize);
	tranView.setMinimumSize(minimumSize);
	
	/*
	 * setDividerLocation(300); //XXX: ignored! bug 4101306
	 * workaround for bug 4101306:
	 */
	
        treeView.setPreferredSize(new Dimension(200, 200));

        setPreferredSize(new Dimension(500, 300));
    }

public synchronized void updateTransactions ()
    {
	if (scanningNode != null)
	{
	    String root = null;
	    String storeImple = null;
	    DefaultMutableTreeNode top = (DefaultMutableTreeNode) topTran.getFirstChild();
	    DefaultTreeModel model = (DefaultTreeModel) transactions.getModel();

	    try
	    {
		ClassName actionStoreType = new ClassName(arjPropertyManager.propertyManager.getProperty(Environment.ACTION_STORE,
									     ArjunaNames.Implementation_ObjectStore_defaultActionStore().stringForm()));
		
		ObjectStore imple = new ObjectStore(actionStoreType, root);
		InputObjectState types = new InputObjectState();

		startSweep();
		
		if (imple.allTypes(types))
		{
		    String fullPathName = null;
		    boolean found = false;
		
		    try
		    {
			boolean endOfList = false;
			DefaultMutableTreeNode currentNode = null;
			DefaultMutableTreeNode currentRoot = top;

			while (!endOfList)
			{
			    fullPathName = types.unpackString();

			    if (fullPathName.compareTo("") == 0)
				endOfList = true;
			    else
			    {
				found = true;
			    
				InputObjectState uids = new InputObjectState();
				String nodeName = stripName(fullPathName);
				boolean added = false;

				currentNode = findNode(fullPathName);

				if (currentNode == null)
				{
				    currentNode = new DefaultMutableTreeNode(nodeName);
				    addDirectory(currentNode, fullPathName);
				    currentRoot.add(currentNode);
				    
				    /*
				     * New, so update view.
				     */

				    int i[] = new int[1];
				    
				    i[0] = currentRoot.getChildCount()-1;
				    
				    model.nodesWereInserted(currentRoot, i);

				    added = true;
				}

				currentRoot = findRoot(top, currentNode);

				if (added)
				    currentRoot.add(currentNode);

				if (imple.allObjUids(fullPathName, uids))
				{
				    Uid theUid = new Uid(Uid.nullUid());
				
				    try
				    {
					boolean endOfUids = false;
					boolean first = true;
					boolean haveUids = false;
					
					while (!endOfUids)
					{
					    theUid.unpack(uids);

					    if (theUid.equals(Uid.nullUid()))
					    {
						if (!haveUids)
						{
						    if (emptyDirectory(currentNode))
						    {
							currentNode.removeAllChildren();
							model.nodeChanged(currentNode);
						    }
						}
						
						endOfUids = true;
					    }
					    else
					    {
						haveUids = true;
						
						if (first)
						{
						    currentNode.removeAllChildren();

						    first = false;
						}
					
						DefaultMutableTreeNode tranID = new DefaultMutableTreeNode(theUid.stringForm());

						tranID.add(new DefaultMutableTreeNode(new String("status: "+statusToString(imple.currentState(theUid, fullPathName)))));

						currentNode.add(tranID);

						added = true;
					    }
					}
				    }
				    catch (Exception e)
				    {
					// end of uids!
				    }

				    if (added)
					model.nodeChanged(currentNode);
				}
			    }

			    /*
			     * To show that we did some work, write some text.
			     */
			
			    if (!found)
				currentRoot.add(emptyTx);
			}
		    }
		    catch (Exception e)
		    {
			// end of list!
		    }
		}

		try
		{
		    endSweep();
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		    System.exit(0);
		}
	    }
	    catch (Exception e)
	    {
		System.err.println(e);
	    }
	}
    }    

private boolean emptyDirectory (DefaultMutableTreeNode node)
    {
	DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getFirstChild();

	if (child != null)
	{
	    DefaultMutableTreeNode status = (DefaultMutableTreeNode) child.getFirstChild();

	    if (status != null)
		return status.isLeaf();
	}

	return true;
    }

private void createNodes (DefaultMutableTreeNode topMachine)
    {
	int number = _machines.size();
        DefaultMutableTreeNode machine = null;
	
	if (number == 0)
	{
	    topMachine.add(new DefaultMutableTreeNode("No machines registered."));
	}
	else
	{
	    String machineName = null;
	    
	    for (int i = 0; i < number; i++)
	    {
		machineName = (String) _machines.elementAt(i);
		machine = new DefaultMutableTreeNode(machineName);
		topMachine.add(machine);
	    }
	}
    }

private void modifyTransactionView ()
    {
	if (transactions.isCollapsed(1))
	    transactions.expandRow(1);
	else
	    transactions.collapseRow(1);
    }

private void removeTransactions ()
    {
	int count = transactions.getRowCount();

	for (int i = count; i > 0; i--)
	{
	    transactions.collapseRow(i);
	    transactions.removeSelectionRow(i);
	}

	topTran.removeAllChildren();
	transactions.repaint();
	_dirs = new Vector();

	DefaultTreeModel model = (DefaultTreeModel) transactions.getModel();

	model.reload();
	
	transactions.repaint();

	scanningNode = null;
    }
    
private synchronized void getTransactions (DefaultMutableTreeNode machineName)
    {
	removeTransactions();

	scanningNode = machineName;
	
	String root = null;
	String storeImple = null;
	DefaultMutableTreeNode top = new DefaultMutableTreeNode(scanningNode);

	try
	{
	    ClassName actionStoreType = new ClassName(arjPropertyManager.propertyManager.getProperty(Environment.ACTION_STORE,
										  ArjunaNames.Implementation_ObjectStore_defaultActionStore().stringForm()));

	    ObjectStore imple = new ObjectStore(actionStoreType, root);
	    InputObjectState types = new InputObjectState();

	    if (imple.allTypes(types))
	    {
		String fullPathName = null;
		boolean found = false;
		
		try
		{
		    boolean endOfList = false;
		    DefaultMutableTreeNode currentNode = null;
		    DefaultMutableTreeNode currentRoot = top;

		    topTran.add(currentRoot);

		    while (!endOfList)
		    {
			fullPathName = types.unpackString();

			if (fullPathName.compareTo("") == 0)
			    endOfList = true;
			else
			{
			    found = true;
			    
			    InputObjectState uids = new InputObjectState();
			    String nodeName = stripName(fullPathName);

			    currentNode = new DefaultMutableTreeNode(nodeName);
			    addDirectory(currentNode, fullPathName);

			    currentRoot = findRoot(top, currentNode);
			    currentRoot.add(currentNode);

			    if (imple.allObjUids(fullPathName, uids))
			    {
				Uid theUid = new Uid(Uid.nullUid());
				
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
					    DefaultMutableTreeNode tranID = new DefaultMutableTreeNode(theUid.stringForm());

					    tranID.add(new DefaultMutableTreeNode(new String("status: "+statusToString(imple.currentState(theUid, fullPathName)))));

					    currentNode.add(tranID);
					}
				    }
				}
				catch (Exception e)
				{
				    // end of uids!
				}
			    }
			}

			/*
			 * To show that we did some work, write some text.
			 */
			
			if (!found)
			    currentRoot.add(emptyTx);
		    }
		}
		catch (Exception e)
		{
		    // end of list!
		}
	    }
	}
	catch (Exception e)
	{
	    System.err.println(e);
	}

	DefaultTreeModel model = (DefaultTreeModel) transactions.getModel();

	model.reload();
	
	transactions.repaint();
    }

private void startSweep ()
    {
	int number = _dirs.size();
	    
	if (number != 0)
	{
	    for (int i = 0; i < number; i++)
	    {
		DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);
		    
		if (dirEntry != null)
		    dirEntry.present = false;
	    }
	}
    }

private void endSweep ()
    {
	int number = _dirs.size();
	    
	if (number != 0)
	{
	    for (int i = 0; i < number; i++)
	    {
		DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);
		    
		if (dirEntry != null)
		{
		    if (!dirEntry.present)
		    {
			DefaultMutableTreeNode top = (DefaultMutableTreeNode) topTran.getFirstChild();
			DefaultTreeModel model = (DefaultTreeModel) transactions.getModel();
			DefaultMutableTreeNode root = findRoot(top, dirEntry.node);

			int index = root.getIndex(dirEntry.node);
			int j[] = new int[1];
			Object o[] = new Object[1];

			j[0] = root.getIndex(dirEntry.node);
			o[0] = dirEntry.node;
			
			root.remove(dirEntry.node);

			model.nodesWereRemoved(root, j, o);

			_dirs.removeElement(dirEntry);
			
			dirEntry.node = null;
			j = null;
			o = null;
		    }
		}
	    }
	}
    }
    
private synchronized DefaultMutableTreeNode findNode (String fullPathName)
    {
	if (fullPathName != null)
	{
	    int number = _dirs.size();
	    
	    if (number != 0)
	    {
		for (int i = 0; i < number; i++)
		{
		    DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);
		    
		    if (dirEntry != null)
		    {
			if (dirEntry.fullPathName.compareTo(fullPathName) == 0)
			{
			    /*
			     * Found entry in new list, so mark it as present.
			     */
			    
			    dirEntry.present = true;
			    
			    return dirEntry.node;
			}
		    }
		}
	    }
	}
	
	return null;
    }
 
private synchronized DefaultMutableTreeNode findRoot (DefaultMutableTreeNode top,
						      DefaultMutableTreeNode curr)
    {
	DefaultMutableTreeNode root = top;
	
	if (curr != null)
	{
	    int number = _dirs.size();
	    String name = fullNodeName(curr);

	    if (number != 0)
	    {
		for (int i = 0; i < number; i++)
		{
		    DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);
		    
		    if (dirEntry != null)
		    {
			DefaultMutableTreeNode node = dirEntry.node;
			String dirName = dirEntry.fullPathName;

			if ((name.indexOf(dirName) != -1) && (node != curr))
			    root = node;
		    }
		}
	    }
	}
	
	return root;
    }

private void addDirectory (DefaultMutableTreeNode node, String path)
    {
	_dirs.addElement(new DirEntry(node, path));
    }

private void removeDirectory (String path)
    {
	Enumeration elements = _dirs.elements();

	while (elements.hasMoreElements())
	{
	    DirEntry e = (DirEntry) elements.nextElement();

	    if (e.fullPathName.compareTo(path) == 0)
	    {
		_dirs.removeElement(e);
		return;
	    }
	}
    }

private String fullNodeName (DefaultMutableTreeNode curr)
    {
	String root = "StateManager";

	if (curr != null)
	{
	    int number = _dirs.size();

	    if (number != 0)
	    {
		for (int i = 0; i < number; i++)
		{
		    DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);

		    if (dirEntry != null)
		    {
			if (dirEntry.node == curr)
			    return dirEntry.fullPathName;
		    }
		}

		root = (String) curr.getUserObject();
	    }
	}

	return root;
    }
    
private String stripName (String name)
    {
	String root = null;
	
	if (name != null)
	{
	    int number = _dirs.size();

	    if (number != 0)
	    {
		for (int i = 0; i < number; i++)
		{
		    DirEntry dirEntry = (DirEntry) _dirs.elementAt(i);

		    if (dirEntry != null)
		    {
			DefaultMutableTreeNode node = dirEntry.node;
			String dirName = dirEntry.fullPathName;
			int subString = name.indexOf(dirName);

			if ((subString != -1) && (name.compareTo(dirName) != 0))
			{
			    root = name.substring(subString+dirName.length()+1);
			}
		    }
		}
	    }

	    if (root == null)
		root = name;
	}
	else
	    root = "StateManager";
	
	return root;
    }    

private void printChildren (DefaultMutableTreeNode currentNode)
    {
	if (currentNode != null)
	{
	    Enumeration children = currentNode.children();

	    if (children != null)
	    {
		String name = (String) currentNode.getUserObject();

		System.out.println("Node: "+name);
		
		while (children.hasMoreElements())
		{
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();

		    name = (String) node.getUserObject();

		    System.out.println("\tChild: "+name);
		}
	    }
	}
    }

private static String statusToString (int status)
    {
	switch (status)
	{
	case ObjectStore.OS_COMMITTED:
	    return "ObjectStore.OS_COMMITTED";
	case ObjectStore.OS_UNCOMMITTED:
	    return "ObjectStore.OS_UNCOMMITTED";
	case ObjectStore.OS_HIDDEN:
	    return "ObjectStore.OS_HIDDEN";
	case ObjectStore.OS_COMMITTED_HIDDEN:
	    return "ObjectStore.OS_COMMITTED_HIDDEN";
	case ObjectStore.OS_UNCOMMITTED_HIDDEN:
	    return "ObjectStore.OS_UNCOMMITTED_HIDDEN";
	default:
	case ObjectStore.OS_UNKNOWN:
	    return "ObjectStore.OS_UNKNOWN";
	}
    }
	    
public static void main (String[] args)
    {
	Uid u = new Uid();
	String timeout = arjPropertyManager.propertyManager.getProperty(pollingTimeout);
	
	if (timeout != null)
	{
	    try
	    {
		sleepTime = Long.parseLong(timeout);  // is it a digit?
	    }
	    catch (NumberFormatException e)
	    {
		System.err.println("Error - specified timeout "+timeout+" is invalid!");
		System.exit(0);
	    }
	}

        /*
         * Create a window. Use JFrame since this window will include 
         * lightweight components.
         */
	
        JFrame frame = new JFrame("OTM Transaction Monitor");

        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };
	
        frame.addWindowListener(l);

	OTM otm = new OTM();
	
        frame.getContentPane().add("Center", otm);
        frame.pack();
        frame.show();

	if (sleepTime != -1)
	{
	    MonitorThread thread = new MonitorThread(otm, sleepTime);
	    
	    thread.start();
	}
    }

private static Vector _machines = new Vector();
private static Vector _dirs = new Vector();
private static DefaultMutableTreeNode topMachine = null;
private static DefaultMutableTreeNode topTran = null; 
private static DefaultMutableTreeNode scanningNode = null;
private static DefaultMutableTreeNode emptyTx = new DefaultMutableTreeNode("No transactions.");
private static JTree tree = null;
private static JTree transactions = null;
private static String pollingTimeout = "MONITORING_TIMEOUT";
private static long sleepTime = -1;

};
