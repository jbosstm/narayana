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
package com.arjuna.ats.tools.objectstorebrowser.entityviewer;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Hashtable;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: EntityViewerRepository.java 2342 2006-03-30 13:06:17Z  $
 */

public class EntityViewerRepository
{
    private final static String VIEWER_PROPERTY_PREFIX = "com.arjuna.mwtools.objectstorebrowser.entityviewers.";

    private static Hashtable    _viewers = new Hashtable();

    public static void registerEntityViewer(String type, EntityViewerInterface entityViewer)
    {
        _viewers.put(type, entityViewer);
    }

    public static EntityViewerInterface getEntityViewer(String type)
    {
        return (EntityViewerInterface)_viewers.get(type);
    }

    public static void initialiseRepository(Properties props)
    {
        try
        {
            Enumeration propertiesEnum = props.propertyNames();

            while ( propertiesEnum.hasMoreElements() )
            {
                String propertyName = (String)propertiesEnum.nextElement();

                if ( propertyName.startsWith( VIEWER_PROPERTY_PREFIX ) )
                {
                    String viewerClassname = props.getProperty( propertyName );
                    /** Get the type name and ensure the delimiters and the correct ones **/
                    String type = propertyName.substring(VIEWER_PROPERTY_PREFIX.length()).replace('/',java.io.File.separatorChar);
                    try
                    {
                        EntityViewerInterface viewer = (EntityViewerInterface)Class.forName( viewerClassname ).newInstance();

                        EntityViewerRepository.registerEntityViewer( type, viewer);
                    }
                    catch (Exception e)
                    {
                        throw new ExceptionInInitializerError("Failed to create instance of '"+viewerClassname+"' entity viewer: "+e);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError("Failed to initiate object state viewers: "+e);
        }
    }
}
