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
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

/**
 * The X_C_TYPE buffer supports the broadest set of parameter types.
 */
public interface X_C_TYPE extends Buffer {

    /**
     * Get the short value identified by the key.
     * 
     * @param key The key to use
     * @return The short value
     * @throws ConnectionException In case the key is not part of the structure.
     */
    public short getShort(String key) throws ConnectionException;

    /**
     * Set the short value
     * 
     * @param key The value to set
     * @param value The value to use
     * @throws ConnectionException In case the key is unknown.
     */
    public void setShort(String key, short value) throws ConnectionException;

    public long getLong(String key) throws ConnectionException;

    public void setLong(String key, long value) throws ConnectionException;

    public byte getByte(String key) throws ConnectionException;

    public void setByte(String key, byte value) throws ConnectionException;

    public short[] getShortArray(String key) throws ConnectionException;

    public void setShortArray(String key, short[] value) throws ConnectionException;

    public long[] getLongArray(String key) throws ConnectionException;

    public void setLongArray(String key, long[] value) throws ConnectionException;

    public byte[] getByteArray(String key) throws ConnectionException;

    public void setByteArray(String key, byte[] value) throws ConnectionException;

    public int getInt(String key) throws ConnectionException;

    public void setInt(String key, int value) throws ConnectionException;

    public float getFloat(String key) throws ConnectionException;

    public void setFloat(String key, float value) throws ConnectionException;

    public double getDouble(String key) throws ConnectionException;

    public void setDouble(String key, double value) throws ConnectionException;

    public int[] getIntArray(String key) throws ConnectionException;

    public void setIntArray(String key, int[] value) throws ConnectionException;

    public float[] getFloatArray(String key) throws ConnectionException;

    public void setFloatArray(String key, float[] value) throws ConnectionException;

    public double[] getDoubleArray(String key) throws ConnectionException;

    public void setDoubleArray(String key, double[] value) throws ConnectionException;

    public byte[][] getByteArrayArray(String key) throws ConnectionException;

    public void setByteArrayArray(String key, byte[][] value) throws ConnectionException;
}
