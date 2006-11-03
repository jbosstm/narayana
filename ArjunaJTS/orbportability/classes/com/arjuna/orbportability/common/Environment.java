/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.common;

import com.arjuna.orbportability.Services;

import java.util.Hashtable;

/**
 * Various property variables which can be set to
 * achieve different effects.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

/*
 * These names had better be unique!
 */

public class Environment
{

public static final String CORBA_DIAGNOSTICS = "com.arjuna.orbportability.corbaDiagnostics";
public static final String INITIAL_REFERENCES_ROOT = "com.arjuna.orbportability.initialReferencesRoot";
public static final String INITIAL_REFERENCES_FILE = "com.arjuna.orbportability.initialReferencesFile";
public static final String FILE_DIR = "com.arjuna.orbportability.fileDir";
public static final String RESOLVE_SERVICE = "com.arjuna.orbportability.resolveService";
public static final String EVENT_HANDLER = "com.arjuna.orbportability.eventHandler";
public static final String ORB_IMPLEMENTATION = "com.arjuna.orbportability.orbImplementation";
public static final String OA_IMPLEMENTATION = "com.arjuna.orbportability.oaImplementation";
public static final String PROPERTIES_FILE = "com.arjuna.orbportability.propertiesFile";
public static final String BIND_MECHANISM= "com.arjuna.orbportability.bindMechanism";
public static final String DEFAULT_ORB_CONFIGURATION = "com.arjuna.orbportability.defaultConfigurationFilename";
}
