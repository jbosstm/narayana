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
 * $Id: GraphPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.perfgraph;

import com.arjuna.ats.tools.perfgraph.graphbean.XYGraphingBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class GraphPanel extends JPanel implements Runnable
{
	private final static long UPDATE_PERIOD = 5000;

	private XYGraphingBean	_graphingBean = null;
	private BufferedImage	_graphImage = null;

	private Thread			_repainter = null;
    private boolean			_repaint = true;
	private int				_updatePeriod = 0;

	/**
	 * Create a new JPanel with a double buffer and a flow layout
	 */
	public GraphPanel(int updatePeriod)
	{
		this.setPreferredSize(new Dimension(400,300));

		/** Create graph bean **/
		_graphingBean = new XYGraphingBean();
        _graphingBean.setXAxisLabel("Sample");
		_graphingBean.setYAxisLabel("Value");

		/** Create initial graph image **/
		_graphImage = _graphingBean.createGraphImage(400,300);

		_updatePeriod = updatePeriod;

		/** Register component listener to resize the image if the panel is resized **/
		this.addComponentListener(new ComponentAdapter() {
			/**
			 * Invoked when the component's size changes.
			 */
			public void componentResized(ComponentEvent e)
			{
				Component c = e.getComponent();

				if ( ( c.getWidth() > 0 ) && ( c.getHeight() > 0 ) )
				{
					_graphImage = _graphingBean.createGraphImage(c.getWidth(), c.getHeight());
				}
			}
		});

		_repainter = new Thread(this);
		_repainter.setName("Repainter-Thread");
		_repainter.start();
	}

	public void stopRepainting()
	{
		_repaint = false;
		_repainter.interrupt();
	}

	public void run()
	{
		while (_repaint)
		{
			try
			{
				Thread.sleep(_updatePeriod);
			}
			catch (Exception e)
			{
		            // Ignore	
			}

			updateGraph();
		}
	}

	public void updateGraph()
	{
		if ( ( this.getWidth() > 0 ) && ( this.getHeight() > 0 ) )
		{
			_graphImage = _graphingBean.createGraphImage(this.getWidth(), this.getHeight());
		}

		repaint();
	}

	public XYGraphingBean getGraphingBean()
	{
		return _graphingBean;
	}

	public void paint(Graphics g)
	{
		g.drawImage(_graphImage, 0, 0, this);
	}

	public void update(Graphics g)
	{
		paint(g);
	}
}
