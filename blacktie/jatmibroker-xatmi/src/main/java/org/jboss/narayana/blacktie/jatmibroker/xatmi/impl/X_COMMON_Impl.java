/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General public  License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General public  License for more details.
 * You should have received a copy of the GNU Lesser General public  License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_COMMON;

/**
 * The X_COMMON buffer type supports a subset of the types provided by the X_C_TYPE buffer in order to support more language
 * portable data exchange.
 */
public class X_COMMON_Impl extends BufferImpl implements X_COMMON {

    /**
     * The default ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The list of types supported by X_COMMON, short, long, byte and arrays of these types.
     */
    private static List<Class> types = new ArrayList<Class>();

    /**
     * Populate the list of supported types
     */
    static {
        Class[] x_commonType = new Class[] { short.class, long.class, byte.class, short[].class, long[].class, byte[].class };
        for (int i = 0; i < x_commonType.length; i++) {
            types.add(x_commonType[i]);
        }
    }

    /**
     * The constructor is hidden as a <code>Connection</code> should be used to allocate the object.
     * 
     * @param subtype The subtype of the buffer, must be registered in the configuration
     * @param properties The properties to use
     * @throws ConnectionException In case the buffer cannot be created.
     * @throws ConfigurationException
     * @see {@link ConnectionImpl#tpalloc(String, String)}
     */
    public X_COMMON_Impl(String subtype) throws ConnectionException, ConfigurationException {
        super("X_COMMON", subtype, true, types);
    }

    /**
     * Get the short value identified by the key.
     * 
     * @param key The key to use
     * @return The short value
     * @throws ConnectionException In case the key is not part of the structure.
     */
    public short getShort(String key) throws ConnectionException {
        return ((Short) getAttributeValue(key, short.class)).shortValue();
    }

    /**
     * Set the short value
     * 
     * @param key The value to set
     * @param value The value to use
     * @throws ConnectionException In case the key is unknown.
     */
    public void setShort(String key, short value) throws ConnectionException {
        setAttributeValue(key, short.class, value);
    }

    public long getLong(String key) throws ConnectionException {
        return ((Long) getAttributeValue(key, long.class)).longValue();
    }

    public void setLong(String key, long value) throws ConnectionException {
        setAttributeValue(key, long.class, value);
    }

    public byte getByte(String key) throws ConnectionException {
        return ((Byte) getAttributeValue(key, byte.class)).byteValue();
    }

    public void setByte(String key, byte value) throws ConnectionException {
        setAttributeValue(key, byte.class, value);
    }

    public short[] getShortArray(String key) throws ConnectionException {
        return (short[]) getAttributeValue(key, short[].class);
    }

    public void setShortArray(String key, short[] value) throws ConnectionException {
        setAttributeValue(key, short[].class, value);
    }

    public long[] getLongArray(String key) throws ConnectionException {
        return (long[]) getAttributeValue(key, long[].class);
    }

    public void setLongArray(String key, long[] value) throws ConnectionException {
        setAttributeValue(key, long[].class, value);
    }

    public byte[] getByteArray(String key) throws ConnectionException {
        return (byte[]) getAttributeValue(key, byte[].class);
    }

    public void setByteArray(String key, byte[] value) throws ConnectionException {
        setAttributeValue(key, byte[].class, value);
    }
}
