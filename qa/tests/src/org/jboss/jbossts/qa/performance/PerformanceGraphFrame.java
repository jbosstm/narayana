/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.performance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PerformanceGraphFrame extends JFrame
{
    private XYGraphingBean _graphingBean;

    public PerformanceGraphFrame(PerformanceLogger p)
    {
        setTitle("Performance Graph ("+p.getDataName()+")");

        _graphingBean = new XYGraphingBean();
        _graphingBean.addDataSeries(createDataSeries(p));
        _graphingBean.setXAxisLabel(p.getXAxisLabel());
        _graphingBean.setYAxisLabel(p.getYAxisLabel());

        this.setSize(400,300);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e)
            {
                super.windowClosing(e);

                System.exit(0);
            }
        });
        show();
    }


    private XYDataSeries createDataSeries(PerformanceLogger logger)
    {
        ArrayList data = logger.getData();
        XYDataSeries series = new XYDataSeries(Color.blue);

        for (int count=0;count<data.size();count++)
        {
            XYData point = (XYData)data.get(count);
            series.addPoint( new XYDataPoint( point.getX(), point.getY() ) );
        }

        return series;
    }


    /**
     * Paints the container. This forwards the paint to any lightweight
     * components that are children of this container. If this method is
     * reimplemented, super.paint(g) should be called so that lightweight
     * components are properly rendered. If a child component is entirely
     * clipped by the current clipping setting in g, paint() will not be
     * forwarded to that child.
     *
     * @param g the specified Graphics window
     * @see   java.awt.Component#update(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        BufferedImage img = _graphingBean.createGraphImage(this.getWidth(), this.getHeight());

        g.drawImage(img,0,0,this);
    }

    /**
     * Just calls <code>paint(g)</code>.  This method was overridden to
     * prevent an unnecessary call to clear the background.
     *
     * @param g the Graphics context in which to paint
     */
    public void update(Graphics g)
    {
        paint(g);
    }
}