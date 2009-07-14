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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PerformanceLogger.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.jbossts.qa.performance;

import java.util.ArrayList;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class PerformanceLogger
{
    private final static String PERF_DATA = "<PERF_DATA";

    private ArrayList _points = new ArrayList();
    private String      _xAxisLabel = "X-Axis";
    private String      _yAxisLabel = "Y-Axis";
    private String      _name = null;

    /**
     * Create a performance logger that logs data of type 'dataName'.
     *
     * @param dataName A unique name which will be used to identify the type of data.
     */
    public PerformanceLogger(String dataName)
    {
        _name = dataName;
    }

    public String getDataName()
    {
        return _name;
    }

    /**
     * Set the label for the X axis.
     *
     * @param label The label for the X-axis.
     */
    public void setXAxisLabel(String label)
    {
        _xAxisLabel = label;
    }

    /**
     * Set the label for the Y axis.
     *
     * @param label The label for the Y-axis.
     */
    public void setYAxisLabel(String label)
    {
        _yAxisLabel = label;
    }

    /**
     * Retrieve a copy of the performance data.
     *
     * @return An array list containing XYData objects.
     */
    public ArrayList getData()
    {
        return (ArrayList)_points.clone();
    }

    /**
     * Add the data x,y to the performance data.
     *
     * @param x The X data.
     * @param y The Y data.
     */
    public void addData(double x, double y)
    {
        _points.add(new XYData(x,y));
    }

    public String getXAxisLabel()
    {
        return _xAxisLabel;
    }

    public String getYAxisLabel()
    {
        return _yAxisLabel;
    }

    /**
     * Output the logger performance data to the given output stream.
     *
     * @param out The stream to log the data to.
     */
    public void output(OutputStream out) throws Exception
    {
        BufferedWriter outStr = new BufferedWriter(new OutputStreamWriter(out));

        outStr.write(PERF_DATA+"{"+_xAxisLabel+"}{"+_yAxisLabel+"}["+_name+"]");

        for (int count=0;count<_points.size();count++)
        {
            XYData data = (XYData)_points.get(count);
            outStr.write(":"+data.getX()+","+data.getY());
        }

        outStr.write(">");
        outStr.close();
    }
}
