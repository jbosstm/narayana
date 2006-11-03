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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XYGraphingBean.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.tools.perfgraph.graphbean;

import com.arjuna.ats.tools.perfgraph.graphbean.XYDataPoint;
import com.arjuna.ats.tools.perfgraph.graphbean.XYDataSeries;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Vector;
import java.util.HashSet;
import java.text.DecimalFormat;

public class XYGraphingBean implements ImageObserver
{
	protected HashSet		_visibleSeries = new HashSet();
    protected Vector        _dataSeries = null;
    protected BufferedImage _image = null;
    protected BufferedImage _yAxisLabel = null;
    protected String        _yAxisLabelText = "Y-Axis";
    protected String        _xAxisLabelText = "X-Axis";
    protected int           _numIncX = 9;
    protected double        _minX = 0;
    protected double        _maxX = 10;
    protected double        _minY = 0;
    protected double        _maxY = 10;
    protected int           _numIncY = 9;

    public XYGraphingBean()
    {
        _dataSeries = new Vector();
    }

    public synchronized void addDataSeries(XYDataSeries data)
    {
        _dataSeries.addElement(data);
        updateGraph();
    }

	public synchronized void removeDataSeries(XYDataSeries data)
	{
		_dataSeries.removeElement(data);
		updateGraph();
	}

	public synchronized void setSeriesVisibility(XYDataSeries data, boolean visible)
	{
		if ( visible )
		{
			_visibleSeries.add(data);
		}
		else
		{
			_visibleSeries.remove(data);
		}
	}

    public synchronized void updateGraph()
    {
		if ( _dataSeries.size() > 0 )
		{
			_minX = Double.MAX_VALUE;
			_maxX = Double.MIN_VALUE;
			_minY = Double.MAX_VALUE;
			_maxY = Double.MIN_VALUE;
		}

		boolean minMaxSet = false;

        for (int count=0;count<_dataSeries.size();count++)
        {
            XYDataSeries d = (XYDataSeries)_dataSeries.elementAt(count);

			if ( _visibleSeries.contains(d) )
			{
				minMaxSet = true;
				_minX = (d.getMinX() < _minX) ? d.getMinX() : _minX;
				_minY = (d.getMinY() < _minY) ? d.getMinY() : _minY;
				_maxX = (d.getMaxX() > _maxX) ? d.getMaxX() : _maxX;
				_maxY = (d.getMaxY() > _maxY) ? d.getMaxY() : _maxY;
			}
        }

		if ( !minMaxSet )
		{
			_minX = 0;
			_maxX = 10;
			_minY = 0;
			_maxY = 10;
		}
    }

    protected BufferedImage createYAxisLabel(Graphics g, String text)
    {
        Rectangle2D labelRect = g.getFontMetrics().getStringBounds(text,g);

        BufferedImage b = new BufferedImage( (int)labelRect.getHeight(), (int)labelRect.getWidth() + 2, BufferedImage.TYPE_INT_ARGB );
        Graphics2D labelGraphics = (Graphics2D)b.getGraphics();
        labelGraphics.setColor(Color.black);
        labelGraphics.rotate( Math.toRadians(90.0) );
        labelGraphics.drawString( text, 0, 0 );

        return b;
    }

    public String getYAxisLabel()
    {
        return(_yAxisLabelText);
    }

    public String getXAxisLabel()
    {
        return(_xAxisLabelText);
    }

    public void setYAxisLabel(String text)
    {
        _yAxisLabelText = text;
    }

    public void setXAxisLabel(String text)
    {
        _xAxisLabelText = text;
    }

    /**
     * Draw the graph
     */
    public synchronized BufferedImage createGraphImage(int width, int height)
    {
        updateGraph();

        BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_USHORT_555_RGB );
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.black);

        double maxLabelWidth = Double.MIN_VALUE;
        double maxLabelHeight = Double.MIN_VALUE;
        DecimalFormat numFormat = new DecimalFormat( "#######.###" );

        for (int count=0;count<=_numIncY;count++)
        {
            String label = numFormat.format(_minY + ((_maxY - _minY)/_numIncY)*(_numIncY - count));
            Rectangle2D labelRect = g.getFontMetrics().getStringBounds(label,g);
            maxLabelWidth = (labelRect.getWidth() > maxLabelWidth) ? labelRect.getWidth() : maxLabelWidth;
        }

        _yAxisLabel = createYAxisLabel( g, getYAxisLabel() );
        Rectangle graphRect = new Rectangle( (int)maxLabelWidth + _yAxisLabel.getWidth() + 5, 50, width - 150, height - 150 );

		/** Draw Legend **/
		g.setColor(Color.black);
		g.drawRect((int)graphRect.getX() + (int)graphRect.getWidth() + 10, (int)graphRect.getY() + 10, 100, (_dataSeries.size() * 12) + 20);
		g.drawString("Legend", (int)graphRect.getX() + (int)graphRect.getWidth() + 12, (int)graphRect.getY() + 22);

		for (int count=0;count<_dataSeries.size();count++)
		{
			XYDataSeries series = (XYDataSeries)_dataSeries.get(count);
			g.setColor(series.getSeriesColour());
			g.drawString( series.getName(), (int)graphRect.getX() + (int)graphRect.getWidth() + 12, (int)graphRect.getY() + 40 + (count*12));
		}

        /**
         * Draw the axis
         *
         *5|                        Pixels / Value
         *0|                        1 / 5
         *5| 1                      1 / (1/5)
         *0|
         *5|    2
         * +--------
         */
        g.setColor( Color.gray );
        g.drawLine( (int)graphRect.getX(), (int)graphRect.getY(), (int)graphRect.getX(), (int)( graphRect.getY() + graphRect.getHeight() ) );
        g.drawLine( (int)graphRect.getX(), (int)( graphRect.getY() + graphRect.getHeight() ), (int)(graphRect.getX() + graphRect.getWidth()), (int)(graphRect.getY() + graphRect.getHeight()) );

        double scaleY = graphRect.getHeight() / (_maxY - _minY); // Pixels per value
        double scaleX = graphRect.getWidth() / (_maxX - _minX);
        double incY = (graphRect.getHeight() / _numIncY);

        for (int count=0;count<=_numIncY;count++)
        {
            String label = numFormat.format(_minY + ((_maxY - _minY)/_numIncY)*(_numIncY - count));
            Rectangle2D labelRect = g.getFontMetrics().getStringBounds(label,g);

            g.setColor(Color.black);
            g.drawString( label, (int)graphRect.getX() - (int)labelRect.getWidth() - 2, (int)(count * incY) + (int)graphRect.getY() + 5 );
            g.drawLine((int)graphRect.getX(), (int)(count * incY) + (int)graphRect.getY(), (int)graphRect.getX() - 2, (int)(count * incY) + (int)graphRect.getY());

            g.setColor(Color.lightGray);
            g.drawLine((int)graphRect.getX() + 1, (int)(count * incY) + (int)graphRect.getY(), (int)graphRect.getMaxX(), (int)(count * incY) + (int)graphRect.getY());
        }

        double incX = (graphRect.getWidth() / _numIncX);
        for (int count=0;count<=_numIncX;count++)
        {
            g.setColor(Color.black);

            BufferedImage img = createYAxisLabel(g, numFormat.format((_minX +((_maxX - _minX)/_numIncX)*(count))));
            maxLabelHeight = (img.getHeight() > maxLabelHeight) ? img.getHeight() : maxLabelHeight;
            g.drawImage( img, (int)(count * incX) + (int)graphRect.getX() - 8, (int)(graphRect.getY() + graphRect.getHeight() + 15),this);
            g.drawLine( (int)(count * incX) + (int)graphRect.getX(), (int)(graphRect.getY() + graphRect.getHeight()),
                        (int)(count * incX) + (int)graphRect.getX(), (int)(graphRect.getY() + graphRect.getHeight()) + 2 );

            g.setColor(Color.lightGray);
            g.drawLine( (int)(count * incX) + (int)graphRect.getX(), (int)(graphRect.getY() + graphRect.getHeight() - 1),
                        (int)(count * incX) + (int)graphRect.getX(), (int)(graphRect.getY()) );
        }

        g.setColor(Color.black);
        Rectangle2D xAxisLabelRect = g.getFontMetrics().getStringBounds(getXAxisLabel(),g);
        g.drawString(getXAxisLabel(), (int)graphRect.getCenterX() - (int)(xAxisLabelRect.getWidth() / 2), height - 5);
        g.drawImage(_yAxisLabel, (int)(graphRect.getX() - maxLabelWidth - 10), (int)graphRect.getCenterY() - _yAxisLabel.getHeight(), this);

        for (int count=0;count<_dataSeries.size();count++)
        {
            XYDataSeries dataSeries = ((XYDataSeries)_dataSeries.elementAt(count));
            XYDataPoint[] points = dataSeries.getDataSeries();
            XYDataPoint previous = null;

			if ( _visibleSeries.contains(dataSeries) )
			{
				if (dataSeries.getBeginAtOrigin())
				{
					previous = new XYDataPoint(_minX,_minY);
				}
				else
				{
					if (points.length!=0)
					{
						previous = points[0];
					}
					else
					{
						previous = new XYDataPoint(_minX, _minY);
					}
				}

				g.setColor(dataSeries.getSeriesColour());

				for (int pCount=0;pCount<points.length;pCount++)
				{
					int xPosition = (int)graphRect.getX() + (int)( ( points[pCount].getX() - _minX ) * scaleX );
					int xPreviousPosition = (int)graphRect.getX() + (int)( ( previous.getX() - _minX ) * scaleX );
					int yPosition = ( (int)graphRect.getY() + (int)graphRect.getHeight() ) - (int)( (points[pCount].getY() - _minY) * scaleY );
					int yPreviousPosition = ( (int)graphRect.getY() + (int)graphRect.getHeight() ) - (int)( (previous.getY() - _minY) * scaleY );
					g.drawLine( xPreviousPosition, yPreviousPosition, xPosition, yPosition );

					previous = points[pCount];
				}
			}
        }

        return(image);
    }

    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int width, int height)
    {
        return false;
    }
}
