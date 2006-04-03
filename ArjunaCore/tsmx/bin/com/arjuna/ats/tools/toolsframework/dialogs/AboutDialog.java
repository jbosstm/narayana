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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: AboutDialog.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.dialogs;

import com.sun.image.codec.jpeg.JPEGCodec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public class AboutDialog extends JDialog implements Runnable
{
	private final static String LOGO_FILENAME = "logo.gif";
    private final static String DIALOG_TITLE = "About..";

	private final static long   DEFAULT_WAIT = 4000;

	private Image _logo;

	/**
	 * Creates a non-modal dialog without a title with the
	 * specifed <code>Frame</code> as its owner.
	 *
	 * @param owner the <code>Frame</code> from which the dialog is displayed
	 */
	public AboutDialog(Frame owner, boolean autoShutdown)
	{
		super(owner);

		this.setTitle(DIALOG_TITLE);

		try
		{
			_logo = createLogo();

			this.getContentPane().add(new LogoPanel(_logo));
		}
		catch (java.io.IOException e)
		{
			JOptionPane.showMessageDialog(this, "Failed to open image file!");
		}

		this.setSize(_logo.getWidth(this),_logo.getHeight(this));

		/** Add mouse listener to close window on click **/
        this.getContentPane().addMouseListener(new MouseAdapter()
		{
			/**
			 * Invoked when a mouse button has been pressed on a component.
			 */
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);

				dispose();
			}
		});

		this.setLocation((int)owner.getLocation().getX() + ( owner.getWidth() / 2) - ( this.getWidth() / 2),
				         (int)owner.getLocation().getY() + ( owner.getHeight() / 2) - ( this.getHeight() / 2) );
		show();

		/** If this window is to be automatically shutdown then start the timer thread **/
		if (autoShutdown)
		{
			Thread t = new Thread(this);
			t.setName("AboutDialogThread");
			t.start();
		}
	}

	public void run()
	{
		// Wait DEFAULT_WAIT milliseconds
		try
		{
			Thread.sleep( DEFAULT_WAIT );
		}
		catch (InterruptedException e)
		{
			// Ignore
		}

		// Close the window
		dispose();
	}

	private Image createLogo() throws java.io.IOException
	{
		URL imageUrl = ClassLoader.getSystemResource(LOGO_FILENAME);
		Image img = getToolkit().getImage(imageUrl);
		MediaTracker mt = new MediaTracker(this);

		mt.addImage(img, 0);

		try
		{
			mt.waitForAll();
		}
		catch (InterruptedException e)
		{
			// Ignore
		}

		return img;
	}

	private class LogoPanel extends JPanel
	{
		private Image	_logo;

		public LogoPanel(Image img)
		{
			_logo = img;
		}

		public void paint(Graphics g)
		{
			g.drawImage(_logo,0,0,this);
		}
	}
}
