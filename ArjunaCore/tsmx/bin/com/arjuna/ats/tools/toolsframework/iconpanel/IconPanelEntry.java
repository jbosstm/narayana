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
 * $Id: IconPanelEntry.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.iconpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class IconPanelEntry extends JPanel
{
	private final Color	OVER_COLOR = Color.decode("#9C7DFF");
	private final Color SELECTED_COLOR = Color.decode("#4F2CC0");
	private final Color UNSELECTED_COLOR = Color.black;

	private IconImage				_icon;
	private boolean					_selected = false;
	private JLabel					_label;
	private ArrayList				_listeners = new ArrayList();
    private Object					_data;

	public IconPanelEntry(Object data, String iconFilename)
	{
		final IconPanelEntry thisEntry = this;

		_data = data;

		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		/** Setup layout and background **/
        this.setBackground(Color.white);

		/** Create and add object view icon **/
		this.add( _icon = new IconImage(iconFilename));

		/** Create and add label panel**/
		JPanel labelPanel = new JPanel();
		labelPanel.setBackground(Color.white);
		this.add( labelPanel );

		/** Create and MBean name label **/
		_label = new JLabel(_data.toString());
		_label.setHorizontalAlignment(JLabel.LEFT);
		_label.setForeground(Color.black);
		labelPanel.add( _label );

		/** Add mouse listener for selection handler **/
		this.addMouseListener(new MouseAdapter()
		{
			/**
			 * Invoked when the mouse enters a component.
			 */
			public void mouseEntered(MouseEvent e)
			{
				super.mouseEntered(e);

				_icon.setOver(true);
				_label.setForeground( isSelected() ? SELECTED_COLOR : OVER_COLOR );
			}

			/**
			 * Invoked when the mouse exits a component.
			 */
			public void mouseExited(MouseEvent e)
			{
				super.mouseExited(e);

				_icon.setOver(false);

				_label.setForeground( isSelected() ? SELECTED_COLOR : UNSELECTED_COLOR );
			}

			/**
			 * Invoked when a mouse button has been pressed on a component.
			 */
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);

				setSelected(!isSelected());

				for (int count=0;count<_listeners.size();count++)
				{
					IconSelectionListener listener = (IconSelectionListener)_listeners.get(count);
                    listener.iconSelected(thisEntry, isSelected());
				}
			}
		});
	}

	public IconImage getIconImage()
	{
		return _icon;
	}

	public void addSelectionListener(IconSelectionListener listener)
	{
		_listeners.add(listener);
	}

	public void setSelected(boolean selected)
	{
		_selected = selected;

		_label.setForeground( _selected ? Color.blue : Color.black );
		_icon.setSelected(selected);
		repaint();
	}

	public boolean isSelected()
	{
		return _selected;
	}

	public String getLabelText()
	{
		return _label.getText();
	}

	public Object getData()
	{
		return _data;
	}
}
