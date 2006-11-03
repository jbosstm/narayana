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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: IconPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.iconpanel;

import com.arjuna.ats.tools.jmxbrowser.JMXBrowserFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class IconPanel extends JPanel implements IconSelectionListener
{
	private final static int 	START_X = 10;
	private final static int	START_Y = 10;

	private final static int	INTER_COMPONENT_X_GAP = 10;
	private final static int	INTER_COMPONENT_Y_GAP = 10;

	private IconPanelEntry _selectedEntry = null;
	private ArrayList _selectionListeners = new ArrayList();
    private int _fixedWidth = 0;

	public IconPanel()
	{
		this(0);
	}

	public IconPanel(int fixedWidth)
	{
		_fixedWidth = fixedWidth;

		/** Setup panel **/
		this.setLayout(null);
		this.setBackground(Color.white);
		this.addComponentListener(new ComponentAdapter() {
			/**
			 * Invoked when the component's size changes.
			 */
			public void componentResized(ComponentEvent e)
			{
				layoutContainer();
			}
		});
	}

	public void layoutContainer()
	{
		int currentX = START_X, currentY = START_Y;
		int lastLargestX = 0;
		int largestComponentWidth = 0;
		int largestComponentHeight = 0;

		Component[] components = this.getComponents();

		for (int count=0;count<components.length;count++)
		{
			Dimension prefSize = components[count].getPreferredSize();

			if ( prefSize.getWidth() > largestComponentWidth )
			{
				largestComponentWidth = (int)prefSize.getWidth();
			}
			if ( prefSize.getHeight() > largestComponentHeight )
			{
				largestComponentHeight = (int)prefSize.getHeight();
			}
		}

		for (int count=0;count<components.length;count++)
		{
			if ( ( currentX + largestComponentWidth + INTER_COMPONENT_X_GAP ) > 800 )
			{
				lastLargestX = currentX;
				currentX = START_X;
				currentY += largestComponentHeight;
			}

			components[count].setLocation( currentX, currentY );
			components[count].setSize( new Dimension(largestComponentWidth,largestComponentHeight) );

			currentX += largestComponentWidth + INTER_COMPONENT_X_GAP;
		}
		setPreferredSize( new Dimension( _fixedWidth > 0 ?  _fixedWidth : ( lastLargestX > 0 ? lastLargestX : currentX ), ( currentY + largestComponentHeight ) ) );
	}

	public void resetIcons()
	{
		this.removeAll();

		layoutContainer();
	}

	public void clearSelection()
	{
		Component[] components = this.getComponents();

		for (int count=0;count<components.length;count++)
		{
			IconPanelEntry entry = (IconPanelEntry)components[count];

			entry.setSelected( false );
		}
	}

	public void iconSelected(IconPanelEntry icon, boolean selected)
	{
		/** Each entry object which is not the one that is selected should be unselected **/
		Component[] components = this.getComponents();

		for (int count=0;count<components.length;count++)
		{
			IconPanelEntry entry = (IconPanelEntry)components[count];

			entry.setSelected( ( entry == icon ) && ( selected ) );
		}

		_selectedEntry = selected ? icon : null;

		/** Inform listeners of selection **/
		for (int count=0;count<_selectionListeners.size();count++)
		{
			IconSelectionListener listener = (IconSelectionListener)_selectionListeners.get(count);

			listener.iconSelected(icon, selected);
		}
	}

	public IconPanelEntry addIcon(IconPanelEntry entry)
	{
		try
		{
			add(entry);

			entry.addSelectionListener(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return entry;
	}

	public IconPanelEntry getSelectedEntry()
	{
		return _selectedEntry;
	}

	public int getIconCount()
	{
		return this.getComponents().length;
	}

	public void addSelectionListener(IconSelectionListener listener)
	{
		_selectionListeners.add(listener);
	}

	public void removeSelectionListener(IconSelectionListener listener)
	{
		_selectionListeners.remove(listener);
	}
}
