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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XYDataSeries.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.tools.perfgraph;

import java.util.ArrayList;
import java.awt.*;

public class XYDataSeries
{
	protected String	_name = null;
    protected ArrayList _points = new ArrayList();
    protected Color     _color = null;
    protected boolean   _beginAtOrigin = false;
    protected double    _minX = Double.MAX_VALUE;
    protected double    _minY = Double.MAX_VALUE;
    protected double    _maxX = Double.MIN_VALUE;
    protected double    _maxY = Double.MIN_VALUE;

    public XYDataSeries(String name, Color color)
    {
        _color = color;
		_name = name;
    }

	public String getName()
	{
		return _name;
	}

    public void setData(ArrayList data)
    {
        _points = data;
    }

    public void setBeginAtOrigin(boolean beginAtOrigin)
    {
        _beginAtOrigin = beginAtOrigin;
    }

    public Color getSeriesColour()
    {
        return _color;
    }

    public boolean getBeginAtOrigin()
    {
        return _beginAtOrigin;
    }

    public void removeFirstPoint()
    {
        if (!_points.isEmpty())
        {
            _points.remove(0);
            reevaluate();
        }
    }

    private void reevaluate()
    {
        _minX = Double.MAX_VALUE;
        _minY = Double.MAX_VALUE;
        _maxX = Double.MIN_VALUE;
        _maxY = Double.MIN_VALUE;

        for (int count=0;count<_points.size();count++)
        {
            XYDataPoint p = (XYDataPoint)_points.get(count);

            _minX = (p.getX() < _minX) ? p.getX() : _minX;
            _maxX = (p.getX() > _maxX) ? p.getX() : _maxX;
            _minY = (p.getY() < _minY) ? p.getY() : _minY;
            _maxY = (p.getY() > _maxY) ? p.getY() : _maxY;
        }
    }

    public void addPoint(XYDataPoint p)
    {
        _points.add(p);

        _minX = (p.getX() < _minX) ? p.getX() : _minX;
        _maxX = (p.getX() > _maxX) ? p.getX() : _maxX;
        _minY = (p.getY() < _minY) ? p.getY() : _minY;
        _maxY = (p.getY() > _maxY) ? p.getY() : _maxY;
    }

    public double getMinX()
    {
        return(_minX);
    }

    public double getMaxX()
    {
        return(_maxX);
    }

    public double getMinY()
    {
        return(_minY);
    }

    public double getMaxY()
    {
        return(_maxY);
    }


    public XYDataPoint[] getDataSeries()
    {
        Object[] points = _points.toArray();
        XYDataPoint[] returnData = new XYDataPoint[points.length];

        System.arraycopy(points,0,returnData,0,points.length);

        return(returnData);
    }
}
