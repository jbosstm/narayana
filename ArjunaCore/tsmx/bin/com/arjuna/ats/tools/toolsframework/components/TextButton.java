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
 * $Id: TextButton.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.components;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class TextButton extends JLabel
{
	private Color		_normal;
	private Color		_over;
	private Color		_down;
	private ArrayList   _listeners = new ArrayList();

	public TextButton(String labelText, Color normal, Color over, Color down)
	{
		super(labelText);

		_normal = normal;
		_over = over;
		_down = down;

		this.setForeground(normal);

		this.addMouseListener(new MouseAdapter(){
			/**
			 * Invoked when the mouse enters a component.
			 */
			public void mouseEntered(MouseEvent e)
			{
				super.mouseEntered(e);

				setForeground(_over);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			/**
			 * Invoked when a mouse button has been pressed on a component.
			 */
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);

				setForeground(_down);
			}

			/**
			 * Invoked when a mouse button has been released on a component.
			 */
			public void mouseReleased(MouseEvent e)
			{
				super.mouseReleased(e);

				setForeground(_over);

				ActionEvent ae = new ActionEvent(this, 0, getText());
				for (int count=0;count<_listeners.size();count++)
				{
					ActionListener al = (ActionListener)_listeners.get(count);

					al.actionPerformed(ae);
				}
			}

			/**
			 * Invoked when the mouse exits a component.
			 */
			public void mouseExited(MouseEvent e)
			{
				super.mouseExited(e);

				setForeground(_normal);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	public void addActionListener(ActionListener al)
	{
		_listeners.add(al);
	}

	/**
	 * Calls the UI delegate's paint method, if the UI delegate
	 * is non-<code>null</code>.  We pass the delegate a copy of the
	 * <code>Graphics</code> object to protect the rest of the
	 * paint code from irrevocable changes
	 * (for example, <code>Graphics.translate</code>).
	 * <p>
	 * If you override this in a subclass you should not make permanent
	 * changes to the passed in <code>Graphics</code>. For example, you
	 * should not alter the clip <code>Rectangle</code> or modify the
	 * transform. If you need to do these operations you may find it
	 * easier to create a new <code>Graphics</code> from the passed in
	 * <code>Graphics</code> and manipulate it. Further, if you do not
	 * invoker super's implementation you must honor the opaque property,
	 * that is
	 * if this component is opaque, you must completely fill in the background
	 * in a non-opaque color. If you do not honor the opaque property you
	 * will likely see visual artifacts.
	 *
	 * @param g the <code>Graphics</code> object to protect
	 * @see #paint
	 * @see ComponentUI
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if ( getForeground() != _normal )
		{
			g.setColor(getForeground());
			g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
		}
	}
}
