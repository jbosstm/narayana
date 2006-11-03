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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StateViewersRepository.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers;

import com.arjuna.ats.tools.objectstorebrowser.PluginClassloader;

import java.util.Hashtable;
import java.io.File;

/**
 * Handles object state viewers.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: StateViewersRepository.java 2342 2006-03-30 13:06:17Z  $
 */
public class StateViewersRepository
{
    private final static String STATE_VIEWER_JAR_PREFIX = "osbv-";
    private final static String JAR_MANIFEST_SECTION_NAME = "arjuna-tools-objectstorebrowser";


    /** Store objectTypeName to stateViewer mappings **/
    private static Hashtable	        _stateViewers = new Hashtable();
    private static Hashtable            _abstractRecordStateViewers = new Hashtable();
    private static StateViewerInterface _defaultStateViewer = null;

    public static void registerStateViewer(String objectTypeName, StateViewerInterface stateViewer) throws ViewerAlreadyRegisteredException
    {
	
	/** If stateviewer already registered then throw an exception **/
	if ( !_stateViewers.containsKey(objectTypeName) )
	{
	    _stateViewers.put( objectTypeName, stateViewer );
	}
	else
	{
	    throw new ViewerAlreadyRegisteredException("A viewer is already registered for object type '"+objectTypeName+"'");
	}
    }

    public static void registerAbstractRecordStateViewer(String type, AbstractRecordStateViewerInterface stateViewer) throws ViewerAlreadyRegisteredException
    {
        /** If the state viewer is already registered then throw an exception **/
        if ( !_abstractRecordStateViewers.containsKey(type) )
        {
            _abstractRecordStateViewers.put( type, stateViewer );
        }
        else
        {
            throw new ViewerAlreadyRegisteredException("A viewer is already registered for object type '"+type+"'");
        }
    }

    public static void setDefaultStateViewer(StateViewerInterface svi)
    {
        _defaultStateViewer = svi;
    }

    public static StateViewerInterface lookupStateViewer(String objectTypeName)
    {
	StateViewerInterface svi = (StateViewerInterface)_stateViewers.get(objectTypeName);

        /** If the object type is not registered then return the default svi **/
        return ( svi == null ) ? _defaultStateViewer : svi;
    }

    public static AbstractRecordStateViewerInterface lookupAbstractRecordStateViewer(String type)
    {
        AbstractRecordStateViewerInterface svi = (AbstractRecordStateViewerInterface)_abstractRecordStateViewers.get(type);

        return svi;
    }

    public static void initialiseRepository(File pluginDir)
    {
	try
	{
	    PluginClassloader classloader = new PluginClassloader(STATE_VIEWER_JAR_PREFIX, null, JAR_MANIFEST_SECTION_NAME, pluginDir);
	    Object[] plugins = classloader.getPlugins();
	    
	    for (int count=0;count<plugins.length;count++)
	    {
                if ( plugins[count] instanceof StateViewerInterface )
                {
                    StateViewerInterface viewer = ((StateViewerInterface)plugins[count]);

                    /** Get the type name and ensure the delimiters and the correct ones **/
                    String type = viewer.getType();

                    StateViewersRepository.registerStateViewer( type, viewer );
                }
                else
		    if ( plugins[count] instanceof AbstractRecordStateViewerInterface )
		    {
			AbstractRecordStateViewerInterface viewer = ((AbstractRecordStateViewerInterface)plugins[count]);

			/** Get the type name and ensure the delimiters and the correct ones **/
			String type = viewer.getType();

			StateViewersRepository.registerAbstractRecordStateViewer( type, viewer );
		    }
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    
	    throw new ExceptionInInitializerError("Failed to initiate object state viewers: "+e);
	}
    }
}
