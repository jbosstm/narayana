/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: PostInitLoader.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.utils;

import java.util.Properties;
import java.util.Enumeration;
import java.lang.ClassLoader;

import java.lang.ClassNotFoundException;

/*
 * This class allows the programmer (and us!) to register classes
 * which must be instantiated after ORB initialisation.
 */

public class PostInitLoader extends InitLoader
{

public PostInitLoader (String name, Object obj)
    {
        super( "PostInitLoader", name, obj );

        initialise();
    }

public static String generateOAPropertyName(String objNameSpace)
    {
        return( objNameSpace + ".oa." + PropertyNamePrefix );
    }

public static String generateOAPropertyName(String objNameSpace, String orbName)
    {
        return( objNameSpace + "." + orbName + ".oa." + PropertyNamePrefix );
    }

public static String generateOAPropertyName(String objNameSpace, String orbName, String oaName)
    {
        return( objNameSpace + "." + orbName + ".oa." + oaName + "." + PropertyNamePrefix );
    }

public static String generateOAPropertyName(String objNameSpace, String orbName, String oaName, String initName)
    {
        return( objNameSpace + "." + orbName + ".oa." + oaName + "." + PropertyNamePrefix + "." + initName);
    }

public static String generateORBPropertyName(String objNameSpace, String orbName, String initName)
    {
        return( objNameSpace + "." + orbName + "." + PropertyNamePrefix + "." + initName);
    }

public static String generateORBPropertyName(String objNameSpace, String orbName)
    {
        return( objNameSpace + "." + orbName + "." + PropertyNamePrefix );
    }

public static String generateORBPropertyName(String objNameSpace)
    {
        return( objNameSpace + "." + PropertyNamePrefix );
    }

public static boolean isPostInitProperty(String propertyName)
    {
        return(propertyName.lastIndexOf(PropertyNamePrefix) == (propertyName.length() - PropertyNamePrefix.length()) );
    }

private static final String PropertyNamePrefix = "PostInit";
}
