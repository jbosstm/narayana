/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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