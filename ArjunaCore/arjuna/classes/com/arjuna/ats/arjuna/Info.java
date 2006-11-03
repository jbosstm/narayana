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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Info.javatmpl 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.common.Configuration;

/**
 * Module specific implementation of the interface object.
 *
 * @author Richard Begg (richard_begg@hp.com)
 * @version $Id: Info.javatmpl 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0
 */

public class Info
{
    public String toString()
    {
        StringBuffer moduleInfo = new StringBuffer();

        moduleInfo.append("<module-info name=\"" + Configuration.getBuildTimeProperty("MODULE") + "\">");
        moduleInfo.append("<source-identifier>" + Configuration.getBuildTimeProperty("SOURCEID") + "</source-identifier>");
        moduleInfo.append("<build-information>" + Configuration.getBuildTimeProperty("BUILDINFO") + "</build-information>");
        moduleInfo.append("<version>" + Configuration.getBuildTimeProperty("VERSION") + "</version>");
        moduleInfo.append("<date>" + Configuration.getBuildTimeProperty("DATE") + "</date>");
        moduleInfo.append("<notes>" + Configuration.getBuildTimeProperty("NOTES") + "</notes>");
        moduleInfo.append("<configuration>");
        moduleInfo.append("<properties-file dir=\""+Configuration.propertiesDir()+"\">"+Configuration.propertiesFile()+"</properties-file>");
	moduleInfo.append("<object-store-root>"+Configuration.objectStoreRoot()+"</object-store-root>");
        moduleInfo.append("</configuration>");
        moduleInfo.append("</module-info>");

        return(moduleInfo.toString());
    }
    
}



