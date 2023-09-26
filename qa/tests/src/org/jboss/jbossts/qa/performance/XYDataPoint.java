/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.qa.performance;

public final class XYDataPoint
{
    protected double    _x, _y;

    public XYDataPoint(double x, double y)
    {
        _x = x;
        _y = y;
    }

    public double getX()
    {
        return _x;
    }

    public double getY()
    {
        return _y;
    }

    public void setLocation(double x, double y)
    {
        _x = x;
        _y = y;
    }
}