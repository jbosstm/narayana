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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
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
    private final static String JTA_JAR_MANIFEST_SECTION_NAME = "arjuna-tools-objectstorebrowser-jta";
    private final static String JTS_JAR_MANIFEST_SECTION_NAME = "arjuna-tools-objectstorebrowser-jts";
    private final static String DEFAULT_ABSTRACT_RECORD_VIEWER = "com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord.AbstractRecordViewer";

    /** Store objectTypeName to stateViewer mappings **/
    private static ConcurrentMap        _stateViewers = new ConcurrentHashMap();
    private static ConcurrentMap _abstractRecordStateViewers = new ConcurrentHashMap();
    private static StateViewerInterface _defaultStateViewer = null;

    public static void registerStateViewer(String objectTypeName, StateViewerInterface stateViewer) throws ViewerAlreadyRegisteredException
    {
	_stateViewers.putIfAbsent( objectTypeName, stateViewer );
	/** If stateviewer already registered then throw an exception **/
        /*
	if ( !_stateViewers.containsKey(objectTypeName) )
	{
	    _stateViewers.put( objectTypeName, stateViewer );
	}
	else
	{
	    throw new ViewerAlreadyRegisteredException("A viewer is already registered for object type '"+objectTypeName+"'");
	}
        */
    }

    public static void registerAbstractRecordStateViewer(String type, AbstractRecordStateViewerInterface stateViewer) throws ViewerAlreadyRegisteredException
    {
        _abstractRecordStateViewers.putIfAbsent(type,stateViewer);
        /** If the state viewer is already registered then throw an exception **/
        /*
        if ( !_abstractRecordStateViewers.containsKey(type) )
        {
            _abstractRecordStateViewers.put( type, stateViewer );
        }
        else
        {
            throw new ViewerAlreadyRegisteredException("A viewer is already registered for object type '"+type+"'");
        }
        */
    }

    public static void setDefaultStateViewer(StateViewerInterface svi)
    {
        _defaultStateViewer = svi;
    }

    public static StateViewerInterface lookupStateViewer(String objectTypeName)
    {
	StateViewerInterface svi = (StateViewerInterface)_stateViewers.get(objectTypeName);

        /** If the object type is not registered then look for a more generic viewer **/
        if (svi == null)
        {
            // determine where the last component starts (assumes objectTypeName != null)
            int i = objectTypeName.substring(0, objectTypeName.length() - 1).lastIndexOf('/');

            if (i == -1)
                return _defaultStateViewer; // no more components so return the default viewer
            else
                return lookupStateViewer(objectTypeName.substring(0, i + 1));
        }
        else
            return svi;
    }

    public static AbstractRecordStateViewerInterface lookupAbstractRecordStateViewer(String type)
    {
        AbstractRecordStateViewerInterface svi = (AbstractRecordStateViewerInterface)_abstractRecordStateViewers.get(type);

        if (svi == null)
            return (AbstractRecordStateViewerInterface)_abstractRecordStateViewers.get(DEFAULT_ABSTRACT_RECORD_VIEWER);
        
        return svi;
    }

    public static void initialiseRepository(String mananifestSection, File pluginDir)
    {
	    PluginClassloader classloader = new PluginClassloader(STATE_VIEWER_JAR_PREFIX, null, mananifestSection, pluginDir);
	    Object[] plugins = classloader.getPlugins();

        for (Object plugin : plugins)
        {
            try
            {
                if (plugin instanceof StateViewerInterface)
                {
                    StateViewerInterface viewer = ((StateViewerInterface) plugin);

                    /** Get the type name and ensure the delimiters and the correct ones **/
                    String type = viewer.getType();

                    StateViewersRepository.registerStateViewer(type, viewer);
                }
                else if (plugin instanceof AbstractRecordStateViewerInterface)
                {
                    AbstractRecordStateViewerInterface viewer = ((AbstractRecordStateViewerInterface) plugin);

                    /** Get the type name and ensure the delimiters and the correct ones **/
                    String type = viewer.getType();

                    StateViewersRepository.registerAbstractRecordStateViewer(type, viewer);
                }
            }
            catch (ViewerAlreadyRegisteredException e)
            {
                // ignore
            }
        }
	}

    public static void initialiseRepository(boolean isJTS, File pluginDir)
    {
        initialiseRepository(JTA_JAR_MANIFEST_SECTION_NAME, pluginDir);

        if (isJTS)
            initialiseRepository(JTS_JAR_MANIFEST_SECTION_NAME, pluginDir);
    }

    public static void disposeRepository()
    {
        _stateViewers.clear();
        _abstractRecordStateViewers.clear();
    }
}
