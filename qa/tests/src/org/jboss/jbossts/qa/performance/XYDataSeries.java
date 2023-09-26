/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.performance;

import java.util.ArrayList;
import java.awt.*;

public class XYDataSeries
{
    protected ArrayList _points = new ArrayList();
    protected Color     _color = null;
    protected boolean   _beginAtOrigin = false;
    protected double    _minX = Double.MAX_VALUE;
    protected double    _minY = Double.MAX_VALUE;
    protected double    _maxX = Double.MIN_VALUE;
    protected double    _maxY = Double.MIN_VALUE;

    public XYDataSeries(Color color)
    {
        _color = color;
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