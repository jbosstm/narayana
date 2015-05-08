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
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_C_TYPE;

/**
 * The X_C_TYPE buffer supports the broadest set of parameter types.
 */
public class X_C_TYPE_Impl extends BufferImpl implements X_C_TYPE {

    /**
     * The default ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The list of types, to contain, int, short, long, byte, float, double and arrays of those types
     */
    private static List<Class> types = new ArrayList<Class>();

    /**
     * Populate the types
     */
    static {
        Class[] x_c_typeType = new Class[] { int.class, short.class, long.class, byte.class, float.class, double.class,
                int[].class, short[].class, long[].class, byte[].class, float[].class, double[].class, byte[][].class };
        for (int i = 0; i < x_c_typeType.length; i++) {
            types.add(x_c_typeType[i]);
        }
    }

    /**
     * The constructor is hidden as the <code>Connection</code> factory method should be used instead. ConnectionImpl#tpalloc(String, String)
     * 
     * @param subtype The subtype of the buffer
     * @throws ConnectionException In case the buffer does not exist
     * @throws ConfigurationException
     */
    public X_C_TYPE_Impl(String subtype) throws ConnectionException, ConfigurationException {
        super("X_C_TYPE", subtype, true, types);
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

    public int getInt(String key) throws ConnectionException {
        return ((Integer) getAttributeValue(key, int.class)).intValue();
    }

    public void setInt(String key, int value) throws ConnectionException {
        setAttributeValue(key, int.class, value);
    }

    public float getFloat(String key) throws ConnectionException {
        return ((Float) getAttributeValue(key, float.class)).floatValue();
    }

    public void setFloat(String key, float value) throws ConnectionException {
        setAttributeValue(key, float.class, value);
    }

    public double getDouble(String key) throws ConnectionException {
        return ((Double) getAttributeValue(key, double.class)).floatValue();
    }

    public void setDouble(String key, double value) throws ConnectionException {
        setAttributeValue(key, double.class, value);
    }

    public int[] getIntArray(String key) throws ConnectionException {
        return (int[]) getAttributeValue(key, int[].class);
    }

    public void setIntArray(String key, int[] value) throws ConnectionException {
        setAttributeValue(key, int[].class, value);
    }

    public float[] getFloatArray(String key) throws ConnectionException {
        return (float[]) getAttributeValue(key, float[].class);
    }

    public void setFloatArray(String key, float[] value) throws ConnectionException {
        setAttributeValue(key, float[].class, value);
    }

    public double[] getDoubleArray(String key) throws ConnectionException {
        return (double[]) getAttributeValue(key, double[].class);
    }

    public void setDoubleArray(String key, double[] value) throws ConnectionException {
        setAttributeValue(key, double[].class, value);
    }

    public byte[][] getByteArrayArray(String key) throws ConnectionException {
        return (byte[][]) getAttributeValue(key, byte[][].class);
    }

    public void setByteArrayArray(String key, byte[][] value) throws ConnectionException {
        setAttributeValue(key, byte[][].class, value);
    }

}
