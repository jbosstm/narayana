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
package com.arjuna.common.util.propertyservice;

/**
 * The various property variables that can be set at
 * runtime to configure the some of the classes within
 * the package.
 *
 * The various values are:
 * <ul>
 * <li> VERBOSE_PROPERTY_MANAGER = com.arjuna.common.util.propertyservice.verbosePropertyManager
 * <li> PROPERTY_MANAGER_IMPLEMENTATION = com.arjuna.common.propertyManagerImplementation
 * </ul>
 *
 * @author Richard Begg (richard.begg@arjun.com)
 * @version $Id: Environment.java 2342 2006-03-30 13:06:17Z  $
 * @since clf-2.0
 */

public class Environment
{
   public static final String VERBOSE_PROPERTY_MANAGER = "com.arjuna.common.util.propertyservice.verbosePropertyManager";
   public static final String OVERRIDING_PLUGIN_CLASSNAME = "com.arjuna.common.util.propertyservice.pluginclassname";
}
