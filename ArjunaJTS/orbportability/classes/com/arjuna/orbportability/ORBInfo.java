/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBInfo.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.internal.utils.SimpleXMLParser;

import java.text.MessageFormat;

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

    public static final ORBData orbData ()
    {
	return _theData;
    }

    private static ORBData          _theData = null;
    private static SimpleXMLParser  _xml = null;

    /**
     * @message com.arjuna.orbportability.ORBInfo.unsupportedorb ORBInfo ORB specific class creation failed - unable to find supported ORB
     * @message com.arjuna.orbportability.ORBInfo.creationfailed ORBInfo ORB specific class creation failed with {0}
     */
    private static final String _versionPackage = "com.arjuna.orbportability.internal.orbspecific.versions";

    static
    {
        String className = null;

        try
        {
            Thread.currentThread().getContextClassLoader().loadClass("org.jacorb.orb.ORB");

            className = _versionPackage+".jacorb_2_0";
        }
        catch (ClassNotFoundException je)
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass("com.sun.corba.se.internal.Interceptors.PIORB");

                className = _versionPackage+".javaidl_1_4";
            }
            catch (ClassNotFoundException joe)
            {
                if (opLogger.loggerI18N.isFatalEnabled())
                {
                    opLogger.loggerI18N.fatal( "com.arjuna.orbportability.ORBInfo.unsupportedorb" );
                }

                ExceptionInInitializerError exceptionInInitializerError =
                        new ExceptionInInitializerError( opLogger.logMesg.getString("com.arjuna.orbportability.ORBInfo.unsupportedorb"));
                exceptionInInitializerError.initCause(joe);
                throw exceptionInInitializerError;
            }
        }

        try
        {
            Class c = Thread.currentThread().getContextClassLoader().loadClass(className);

            _theData = (ORBData) c.newInstance();

            _xml = new SimpleXMLParser(_theData.getORBdata() );
        }
        catch (Exception e)
        {

            if (opLogger.loggerI18N.isFatalEnabled())
            {
                opLogger.loggerI18N.fatal( "com.arjuna.orbportability.ORBInfo.creationfailed",
                        new Object[] { e } );
            }

            ExceptionInInitializerError exceptionInInitializerError = new ExceptionInInitializerError(
                    MessageFormat.format(opLogger.logMesg.getString("com.arjuna.orbportability.ORBInfo.creationfailed"),
                            new Object[] { e } ));
            exceptionInInitializerError.initCause(e);
            throw exceptionInInitializerError;
        }
    }

}
