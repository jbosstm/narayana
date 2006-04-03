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
 * $Id: JMXBrowserFrame.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser;

import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanel;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconImage;
import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDetailsPanel;
import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDetailsSupplier;
import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDomainGroup;
import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.tsmx.agent.exceptions.AgentNotFoundException;

import javax.swing.*;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;
import java.util.Iterator;
import java.util.Hashtable;

public class JMXBrowserFrame extends JInternalFrame implements MBeanDetailsSupplier, IconSelectionListener
{
	private final static String JMX_BROWSER_FRAME_TITLE = "JMX Browser";
	private final static String MBEAN_ICON_IMAGE_FILENAME_ATTRIBUTE = "IconFilename";
	private final static String DEFAULT_ICON_FILENAME = "mbean-icon.gif";

	private JPanel				_mbeanPanel;
	private MBeanDetailsPanel   _detailsPanel;
    private Hashtable			_domainPanels = new Hashtable();
	private IconPanelEntry		_selectedIcon = null;

	public JMXBrowserFrame()
	{
		super( JMX_BROWSER_FRAME_TITLE, true, true, true, true );
		/** Create JMX layer **/
		TransactionServiceMX.getTransactionServiceMX();

		/** Setup frame **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.getContentPane().setBackground(Color.white);
		this.getContentPane().setLayout(gbl);

    	_mbeanPanel = new JPanel();
		_mbeanPanel.setLayout(new BoxLayout(_mbeanPanel, BoxLayout.Y_AXIS));
		_mbeanPanel.setBackground(Color.white);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane scroller = new JScrollPane(_mbeanPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		gbl.setConstraints(scroller, gbc);
		this.getContentPane().add(scroller);

		_detailsPanel = new MBeanDetailsPanel(this);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.1;
		gbc.weighty = 1.0;
		gbl.setConstraints(_detailsPanel, gbc);
		this.getContentPane().add(_detailsPanel);

		/** Create the JMX MBean icons **/
		createIcons();

		pack();
		setVisible(true);

		setSize(400,300);
	}

	private void createIcons()
	{
		Set mbeanNames = null;

		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try
		{
			mbeanNames = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().queryNames(null, null);
		}
		catch (AgentNotFoundException e)
		{
			JOptionPane.showMessageDialog( this, "Failed to retrieve JMX Agent reference", "Error", JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}

		Iterator i = mbeanNames.iterator();

		while ( i.hasNext() )
		{
			ObjectName mbeanName = ((ObjectName)i.next());

			addMbean(mbeanName);
		}

		layoutAllGroups();

		_detailsPanel.updateAll();

		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private String getIconFilename(ObjectName on)
	{
		String iconFilename = DEFAULT_ICON_FILENAME;

		try
		{
			iconFilename = (String)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(on, MBEAN_ICON_IMAGE_FILENAME_ATTRIBUTE);
		}
		catch (Exception e)
		{
			// Ignore
		}

		if ( iconFilename == null )
		{
			iconFilename = DEFAULT_ICON_FILENAME;
		}

		return iconFilename;
	}

	private void addMbean(ObjectName on)
	{
		MBeanDomainGroup domainGroup = (MBeanDomainGroup)_domainPanels.get( on.getDomain() );

		if ( domainGroup == null )
		{
			domainGroup = new MBeanDomainGroup( on.getDomain() );
			_domainPanels.put( on.getDomain(), domainGroup);
			_mbeanPanel.add( domainGroup );
		}

		IconPanelEntry ipe = domainGroup.getIconPanel().addIcon( new IconPanelEntry( new MBeanWrapper( on ), getIconFilename( on ) ) );
		ipe.addSelectionListener(this);
	}

	private void layoutAllGroups()
	{
		Component[] c = _mbeanPanel.getComponents();
        int y = 0;

		for (int count=0;count<c.length;count++)
		{
        	MBeanDomainGroup group = (MBeanDomainGroup)c[count];

			group.getIconPanel().layoutContainer();
		}
	}

	public int getNumberOfMBeans()
	{
		int beanCount = 0;

		if ( _domainPanels != null )
		{
			Iterator domainPanelIterator = _domainPanels.values().iterator();

			while ( domainPanelIterator.hasNext() )
			{
				MBeanDomainGroup group = (MBeanDomainGroup)domainPanelIterator.next();
				beanCount += group.getIconPanel().getIconCount();
			}
		}

		return beanCount;
	}

	public IconImage getSelectedMBeanIconImage()
	{
		if ( _selectedIcon != null )
		{
			return _selectedIcon.getIconImage();
		}

		return null;
	}

	public ObjectName getSelectedMBeanName()
	{
		if ( _selectedIcon != null )
		{
			MBeanWrapper selected = (MBeanWrapper)(_selectedIcon.getData());

			return selected.getObjectName();
		}

		return null;
	}

	public MBeanInfo getSelectedMBean()
	{
		MBeanInfo mbeanInfo = null;
		if ( _selectedIcon != null )
		{
			MBeanWrapper selected = (MBeanWrapper)(_selectedIcon.getData());

			try
			{
				mbeanInfo = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getMBeanInfo(selected.getObjectName());
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
				JOptionPane.showMessageDialog(this,"Failed to retrieve mbean information","Error",JOptionPane.ERROR_MESSAGE);
			}
		}

		return mbeanInfo;
	}

	public void iconSelected(IconPanelEntry icon, boolean selected)
	{
		if ( selected )
		{
			Iterator domainPanelIterator = _domainPanels.values().iterator();

			while ( domainPanelIterator.hasNext() )
			{
				MBeanDomainGroup group = (MBeanDomainGroup) domainPanelIterator.next();

				group.clearSelection();
			}

			icon.setSelected(true);
			_selectedIcon = icon;
		}
		else
		{
			_selectedIcon = null;
		}

		_detailsPanel.updateMBeanSpecific();
	}

	private class MBeanWrapper
	{
		private ObjectName _objName;

		public MBeanWrapper(ObjectName objName)
		{
			_objName = objName;
		}

		public ObjectName getObjectName()
		{
			return _objName;
		}

		public String toString()
		{
			return _objName.getKeyPropertyListString();
		}
	}
}
