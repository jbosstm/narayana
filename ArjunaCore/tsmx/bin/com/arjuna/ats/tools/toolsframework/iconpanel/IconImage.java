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
 * $Id: IconImage.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.iconpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.net.URL;

public class IconImage extends JComponent
{
	private Image	_iconImageSelected;
	private Image	_iconImageOver;
	private Image	_iconImageUnselected;
	private boolean _selected = false;
	private boolean _over = false;

	public IconImage(String iconFilename)
	{
		this.setBackground(Color.white);

		setImage(iconFilename);
	}

	public void setImage(Image img)
	{
		_iconImageSelected = img;
		_iconImageOver = _iconImageSelected;
		_iconImageUnselected = getUnselectedImage(_iconImageSelected);
		setPreferredSize(new Dimension(_iconImageSelected.getWidth(this), _iconImageSelected.getHeight(this)));
	}

	public void setImage(String iconFilename)
	{
		try
		{
			MediaTracker mt = new MediaTracker(this);

			URL imageURL = ClassLoader.getSystemResource(iconFilename);

			if ( imageURL != null )
			{
				_iconImageSelected = getToolkit().getImage(imageURL);

				mt.addImage(_iconImageSelected, 0);

				mt.waitForAll();

				_iconImageOver = _iconImageSelected;
				_iconImageUnselected = getUnselectedImage(_iconImageSelected);
			}
		}
		catch (InterruptedException e)
		{
			// Ignore
		}

		setPreferredSize(new Dimension(_iconImageSelected.getWidth(this), _iconImageSelected.getHeight(this)));
	}

	/**
	 * Create selected image which is grayscale.
	 * @param img
	 * @return
	 */
	private Image getUnselectedImage(Image img)
	{
		BufferedImage bi = new BufferedImage(img.getWidth(this), img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(img, 0, 0, this);

		for (int y=0;y<bi.getHeight();y++)
		{
			for (int x=0;x<bi.getWidth();x++)
			{
				int rgb = bi.getRGB(x,y);

				int s = (rgb >> 24) & 0xFF;
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = (rgb >> 8) & 0xFF;
				int l = (int) ( ( (float)( red + green + blue ) / 768 ) * 255 );

				rgb = (s << 24) | (l << 16) | ( l << 8 ) | ( l);

				bi.setRGB(x,y,rgb);
			}
		}

		return bi;
	}

	public boolean isSelected()
	{
		return _selected;
	}

	public void setSelected(boolean selected)
	{
		_selected = selected;
	}

	public void setOver(boolean over)
	{
		_over = over;

		repaint();
	}

	public boolean isOver()
	{
		return _over;
	}

	public void paintComponent(Graphics g)
	{
		if ( _iconImageSelected != null )
		{
			if (_selected)
			{
				g.drawImage( _iconImageSelected, (getWidth() / 2) - (_iconImageSelected.getWidth(this) / 2), (getHeight() / 2) - (_iconImageSelected.getHeight(this) / 2), this);
			}
			else
			{
				if ( _over )
				{
					g.drawImage( _iconImageOver, (getWidth() / 2) - (_iconImageOver.getWidth(this) / 2), (getHeight() / 2) - (_iconImageOver.getHeight(this) / 2), this);
				}
				else
				{
					g.drawImage( _iconImageUnselected, (getWidth() / 2) - (_iconImageUnselected.getWidth(this) / 2), (getHeight() / 2) - (_iconImageUnselected.getHeight(this) / 2), this);
				}
			}
		}
	}

	public Image getImage()
	{
		return _iconImageSelected;
	}
}
