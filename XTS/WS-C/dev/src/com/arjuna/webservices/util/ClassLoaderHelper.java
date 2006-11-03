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
package com.arjuna.webservices.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Utility class for classloading.
 * @author kevin
 */
public class ClassLoaderHelper
{
    /**
     * Attempt to load the named class.
     * @param caller The caller's class.
     * @param className The name of the class.
     * @return The class
     * @throws ClassNotFoundException If the class cannot be found.
     */
    public static Class forName(final Class caller, final String className)
        throws ClassNotFoundException
    {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader() ;
        if (contextClassLoader != null)
        {
            try
            {
                return contextClassLoader.loadClass(className) ;
            }
            catch (final ClassNotFoundException cnfe) {} // Ignore
        }
        final ClassLoader callerClassLoader = caller.getClassLoader() ;
        try
        {
            return callerClassLoader.loadClass(className) ;
        }
        catch (final ClassNotFoundException cnfe) {} // Ignore
        return ClassLoader.getSystemClassLoader().loadClass(className) ;
    }
    
    /**
     * Get the specified resource as a string.
     * @param caller The caller's class.
     * @param resource The resource name.
     * @return The string or null if not found.
     * @throws IOException for read errors.
     */
    public static String getResourceAsString(final Class caller, final String resource)
        throws IOException
    {
        final InputStream is = getResourceAsStream(caller, resource) ;
        if (is == null)
        {
            return null ;
        }
        
        final Reader reader = new InputStreamReader(is) ;
        final StringBuffer stringBuffer = new StringBuffer() ;
        final char[] buffer = new char[1024] ;
        while(true)
        {
            final int count = reader.read(buffer) ;
            if (count == -1)
            {
                break ;
            }
            stringBuffer.append(buffer, 0, count) ;
        }
        return stringBuffer.toString() ;
    }
    
    /**
     * Get the specified resource as an input stream.
     * @param caller The caller's class.
     * @param resource The resource name.
     * @return The input stream or null if not found.
     */
    public static InputStream getResourceAsStream(final Class caller, final String resource)
    {
        if ((resource == null) || (resource.length() == 0))
        {
            return null ;
        }
        
        final String absoluteResource ;
        if (resource.charAt(0) == '/')
        {
            absoluteResource = resource ;
        }
        else
        {
            final String callerName = caller.getName() ;
            final int lastSeparator = callerName.lastIndexOf('.') ;
            if (lastSeparator == -1)
            {
                absoluteResource = '/' + resource ;
            }
            else
            {
                absoluteResource = '/' + callerName.substring(0, lastSeparator+1).replace('.', '/') + resource ; 
            }
        }
        final URL url = getResourceAsURL(caller, absoluteResource) ;
        if (url != null)
        {
            try
            {
                return url.openStream() ;
            }
            catch (final IOException ioe) {}
        }
        return null ;
    }
    
    /**
     * Get the specified resource as a URL.
     * @param caller The caller's class.
     * @param resource The resource name.
     * @return The URL or null if not found.
     */
    public static URL getResourceAsURL(final Class caller, final String resource)
    {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader() ;
        if (contextClassLoader != null)
        {
            final URL contextURL = contextClassLoader.getResource(resource) ;
            if (contextURL != null)
            {
                return contextURL ;
            }
        }
        final URL callerURL = caller.getResource(resource) ;
        if (callerURL != null)
        {
            return callerURL ;
        }
        return ClassLoader.getSystemResource(resource) ;
    }
}
