/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.SimpleXMLParser;
import com.arjuna.orbportability.logging.opLogger;

/**
 * This class queries the ORB specific ORBData object for information
 * about the ORB.
 */

public class ORBInfo
{
    private final static String NAME_ELEMENT = "name";
    private final static String VERSION_ELEMENT = "version";
    private final static String CORBA_VERSION_ELEMENT = "corba-version";
    private final static String MAJOR_ELEMENT = "major";
    private final static String MINOR_ELEMENT = "minor";


    public static final String getInfo()
    {
        return _theData.getORBdata();
    }

    public static final int getOrbEnumValue()
    {
        return ORBType.getORBEnum( getOrbName() );
    }

    public static final String getOrbName()
    {
        String name = "";

        try
        {
            name = _xml.getElementString( NAME_ELEMENT );
        }
        catch (Exception e)
        {
            // Ignore
        }

        return name;
    }

    public static final int getOrbMajorVersion()
    {
        int majorVersion = -1;

        try
        {
            SimpleXMLParser versionParser = _xml.getElementParser( VERSION_ELEMENT );

            majorVersion = Integer.parseInt( versionParser.getElementString( MAJOR_ELEMENT ) );
        }
        catch (Exception e)
        {
            // Ignore
        }

        return majorVersion;
    }

    public static final int getOrbMinorVersion()
    {
        int minorVersion = -1;

        try
        {
            SimpleXMLParser versionParser = _xml.getElementParser( VERSION_ELEMENT );

            minorVersion = Integer.parseInt( versionParser.getElementString( MINOR_ELEMENT ) );
        }
        catch (Exception e)
        {
            // Ignore
        }

        return minorVersion;
    }

    public static final int getCorbaMajorVersion()
    {
        int majorVersion = -1;

        try
        {
            SimpleXMLParser versionParser = _xml.getElementParser( CORBA_VERSION_ELEMENT );

            majorVersion = Integer.parseInt( versionParser.getElementString( MAJOR_ELEMENT ) );
        }
        catch (Exception e)
        {
            // Ignore
        }

        return majorVersion;
    }

    public static final int getCorbaMinorVersion()
    {
        int minorVersion = -1;

        try
        {
            SimpleXMLParser versionParser = _xml.getElementParser( CORBA_VERSION_ELEMENT );

            minorVersion = Integer.parseInt( versionParser.getElementString( MINOR_ELEMENT ) );
        }
        catch (Exception e)
        {
            // Ignore
        }

        return minorVersion;
    }

    private static ORBData          _theData = null;
    private static SimpleXMLParser  _xml = null;

    /**
     * Static block is used to initialize _theData and _xml static variables.
     * They are later used to receive runtime ORB's information such as name or version.
     */
    static
    {
        try {
            _theData = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbData();
            _xml = new SimpleXMLParser(_theData.getORBdata() );
        }
        catch (Exception e)
        {
            opLogger.i18NLogger.fatal_ORBInfo_creationfailed(e);
            throw new ExceptionInInitializerError( e );
        }
    }
}