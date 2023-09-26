/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.utils;


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