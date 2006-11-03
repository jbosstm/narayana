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
 * $Id: JMXMBeanBrowser.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser;

import javax.swing.*;
import java.awt.*;

public class JMXMBeanBrowser extends JPanel
{
	private final static int 	START_X = 10;
	private final static int	START_Y = 10;

	private final static int	INTER_COMPONENT_X_GAP = 10;
	private final static int	INTER_COMPONENT_Y_GAP = 10;

	public JMXMBeanBrowser()
	{

	}

	private void layoutContainer()
	{
		int currentX = START_X, currentY = START_Y;
		int lastLargestY = 0;

		Component[] components = this.getComponents();

		for (int count=0;count<components.length;count++)
		{
			Dimension prefSize = components[count].getPreferredSize();

			if ( ( currentX + prefSize.getWidth() + INTER_COMPONENT_X_GAP ) > this.getWidth() )
			{
				currentX = START_X;
				currentY += lastLargestY;

				lastLargestY = 0;
			}

			components[count].setLocation( currentX, currentY );
			components[count].setSize( prefSize );

			currentX += components[count].getPreferredSize().getWidth() + INTER_COMPONENT_X_GAP;

			if ( prefSize.getHeight() > lastLargestY )
			{
				lastLargestY = (int)prefSize.getHeight() + INTER_COMPONENT_Y_GAP;
			}
		}

		repaint();
	}

}
