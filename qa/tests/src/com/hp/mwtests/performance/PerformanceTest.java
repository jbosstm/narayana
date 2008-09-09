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
package com.hp.mwtests.performance;

public abstract class PerformanceTest
{
    private String[]    _configs = null;
    private String[]    _params = null;

    protected abstract void work() throws Exception;

    public void setServiceConfigs( String[] configs )
    {
        _configs = configs;
    }

    void setParameters(String[] params)
    {
        _params = params;
    }

    public boolean isParameterDefined(String param)
    {
        for (int count=0;count<_params.length;count++)
        {
            if ( param.equals( _params[count] ) )
            {
                return true;
            }
        }

        return false;
    }

    public String[] getParameters()
    {
        return _params;
    }

    public String getServiceConfig( int index )
    {
        return _configs[index];
    }

    public void performWork( int numberOfIterations ) throws Exception
    {
        for (int count=0;count<numberOfIterations;count++)
        {
            work();
        }
    }
}
