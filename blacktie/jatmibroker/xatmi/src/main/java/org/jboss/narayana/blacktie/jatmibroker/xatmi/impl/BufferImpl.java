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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.AttributeStructure;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.BufferStructure;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;

/**
 * This class is used to send and receive data to and from clients to services.
 * 
 * @see X_OCTET_Impl
 * @see X_C_TYPE_Impl
 * @see X_COMMON_Impl
 */
public abstract class BufferImpl implements Serializable, Buffer {
    /**
     * A none-default id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The agreed size of a byte.
     */
    public static final int BYTE_SIZE = 1;

    /**
     * The agreed size of a long.
     */
    public static final int LONG_SIZE = 8;

    /**
     * The agreed size of a int.
     */
    public static final int INT_SIZE = 4;

    /**
     * The agreed size of a short.
     */
    public static final int SHORT_SIZE = 2;

    /**
     * The agreed size of a float.
     */
    public static final int FLOAT_SIZE = INT_SIZE;

    /**
     * The agreed size of a double.
     */
    public static final int DOUBLE_SIZE = LONG_SIZE;

    /**
     * The structure of the buffer.
     */
    private Map<String, Object> structure = new HashMap<String, Object>();

    /**
     * The supported keys.
     */
    private String[] keys;

    /**
     * The supported types
     */
    private Class[] types;

    /**
     * The length of each array in the buffer.
     */
    private int[] lengths;

    /**
     * Has the buffer been deserialized.
     */
    private boolean deserialized;

    /**
     * Is the buffer formatted.
     */
    private boolean formatted;

    /**
     * The current read position.
     */
    int currentPos = 0;

    /**
     * The list of types supported by this class of buffer.
     * 
     * @see X_OCTET_Impl
     * @see X_C_TYPE_Impl
     * @see X_COMMON_Impl
     */
    private List<Class> supportedTypes;

    /**
     * Does the buffer require serialization? i.e. is it not an X_OCTET
     */
    private boolean requiresSerialization;

    /**
     * The type of the buffer? X_OCTET, X_C_TYPE, X_COMMON
     */
    private String type;

    /**
     * The subtype
     */
    private String subtype;

    /**
     * The raw data.
     */
    private byte[] data;

    /**
     * The number of arrays arrays.
     */
    private int[] counts;

    /**
     * The structure of the message.
     */
    private Map<String, Class> format = new HashMap<String, Class>();

    protected int len = -1;

    /**
     * Create a new buffer.
     * 
     * @param type The type of the buffer
     * @param subtype The subtype of the buffer
     * @param requiresSerialization Is the buffer not an X_OCTET?
     * @param supportedTypes The types supported by the buffer, see the individual buffers for more details
     * @param properties The properties to use.
     * @throws ConfigurationException
     * @throws ConnectionException If the buffer is not supported.
     * @see {@link X_OCTET_Impl}
     * @see {@link X_C_TYPE_Impl}
     * @see {@link X_COMMON_Impl}
     */
    BufferImpl(String type, String subtype, boolean requiresSerialization, List<Class> supportedTypes) 
    		throws ConfigurationException, ConnectionException {
        this.type = type;
        this.subtype = subtype;
        this.requiresSerialization = requiresSerialization;
        this.supportedTypes = supportedTypes;

        if (requiresSerialization) {
        	Properties properties = ConnectionFactory.getConnectionFactory().getProperties();
            Map<String, BufferStructure> buffers = (Map<String, BufferStructure>) properties.get("blacktie.domain.buffers");
            BufferStructure buffer = buffers.get(subtype);
            if (buffer == null) {
                throw new ConfigurationException("Subtype was not registered: " + subtype);
            }
            this.len = buffer.wireSize;
            String[] ids = new String[buffer.attributes.size()];
            Class[] types = new Class[buffer.attributes.size()];
            int[] length = new int[buffer.attributes.size()];
            int[] count = new int[buffer.attributes.size()];
            Iterator<AttributeStructure> iterator = buffer.attributes.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                AttributeStructure attribute = iterator.next();
                ids[i] = attribute.id;
                types[i] = attribute.type;
                if (!supportedTypes.contains(types[i])) {
                    throw new ConfigurationException("Cannot use type configured in buffer " + types[i]);
                }
                length[i] = attribute.length;
                count[i] = attribute.count;
                i++;
            }
            format(ids, types, length, count);
        } else {
            this.len = 0;
            format.put("X_OCTET", byte[].class);
        }
    }

    /**
     * Get the format of the message.
     * 
     * @return The format of the message
     */
    public Map<String, Class> getFormat() {
        return format;
    }

    /**
     * Format the buffer.
     * 
     * @param keys The keys
     * @param types The types
     * @param lengths The lengths
     * @param counts The number of each array
     * @throws ConnectionException In case the buffer does not match the format.
     */
    private void format(String[] keys, Class[] types, int[] lengths, int[] counts) throws ConnectionException {
        structure.clear();
        if (keys.length != types.length || types.length != lengths.length) {
            throw new ConnectionException(ConnectionImpl.TPEINVAL, "Invalid format, each array description should be same length");
        }
        this.keys = keys;
        this.types = types;
        this.lengths = lengths;
        this.counts = counts;

        for (int i = 0; i < keys.length; i++) {
            format.put(keys[i], types[i]);
        }
        formatted = true;
    }

    /**
     * Deserialize the buffer.
     * 
     * @param data The data to deserialize.
     * @throws ConnectionException In case the data does not match the format defined.
     */
    public void deserialize(byte[] data) throws ConnectionException {
        currentPos = 0;
        if (requiresSerialization) {
            if (!deserialized && data != null) {
                if (keys == null) {
                    throw new ConnectionException(ConnectionImpl.TPEITYPE, "Message format not provided");
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                for (int i = 0; i < types.length; i++) {
                    if (!supportedTypes.contains(types[i])) {
                        throw new ConnectionException(ConnectionImpl.TPEITYPE, "Cannot read type from buffer " + types[i]);
                    }

                    try {
                        if (types[i] == int.class) {
                            structure.put(keys[i], readInt(dis));
                        } else if (types[i] == short.class) {
                            structure.put(keys[i], readShort(dis));
                        } else if (types[i] == long.class) {
                            structure.put(keys[i], readLong(dis));
                        } else if (types[i] == byte.class) {
                            structure.put(keys[i], readByte(dis));
                        } else if (types[i] == float.class) {
                            structure.put(keys[i], readFloat(dis));
                        } else if (types[i] == double.class) {
                            structure.put(keys[i], readDouble(dis));
                        } else if (types[i] == int[].class) {
                            int[] toRead = new int[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readInt(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == short[].class) {
                            short[] toRead = new short[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readShort(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == long[].class) {
                            long[] toRead = new long[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readLong(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == byte[].class) {
                            byte[] toRead = new byte[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readByte(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == float[].class) {
                            float[] toRead = new float[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readFloat(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == double[].class) {
                            double[] toRead = new double[lengths[i]];
                            for (int j = 0; j < lengths[i]; j++) {
                                toRead[j] = readDouble(dis);
                            }
                            structure.put(keys[i], toRead);
                        } else if (types[i] == byte[][].class) {
                            byte[][] toRead = new byte[counts[i]][lengths[i]];
                            for (int k = 0; k < counts[i]; k++) {
                                for (int j = 0; j < lengths[i]; j++) {
                                    toRead[k][j] = readByte(dis);
                                }
                            }
                            structure.put(keys[i], toRead);
                        } else {
                            throw new ConnectionException(ConnectionImpl.TPEITYPE, "Could not deserialize: " + types[i]);
                        }
                    } catch (IOException e) {
                        throw new ConnectionException(ConnectionImpl.TPEITYPE, "Could not parse the value as: " + keys[i]
                                + " was not a " + types[i] + " and even if it was an array of that type its length was not: "
                                + lengths[i]);
                    }
                }
            }
        } else {
            this.data = data;
            this.len = data.length;
        }
        deserialized = true;
    }

    /**
     * Serialize the buffer.
     * 
     * @return The byte array for sending.
     * @throws ConnectionException In case the data cannot be formatted correctly
     */
    public byte[] serialize() throws ConnectionException {
        currentPos = 0;
        byte[] toReturn = null;
        if (requiresSerialization) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int i = 0; i < types.length; i++) {

                try {
                    if (types[i] == int.class) {
                        Integer toWrite = (Integer) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeInt(dos, toWrite);
                        } else {
                            writeInt(dos, 0);
                        }
                    } else if (types[i] == short.class) {
                        Short toWrite = (Short) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeShort(dos, toWrite);
                        } else {
                            writeShort(dos, (short) 0);
                        }
                    } else if (types[i] == long.class) {
                        Long toWrite = (Long) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeLong(dos, toWrite);
                        } else {
                            writeLong(dos, 0);
                        }
                    } else if (types[i] == byte.class) {
                        Byte toWrite = (Byte) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeByte(dos, toWrite);
                        } else {
                            // writeByte(dos, '\0');
                            writeByte(dos, (byte) 0);
                        }
                    } else if (types[i] == float.class) {
                        Float toWrite = (Float) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeFloat(dos, toWrite);
                        } else {
                            writeFloat(dos, 0);
                        }
                    } else if (types[i] == double.class) {
                        Double toWrite = (Double) structure.get(keys[i]);
                        if (toWrite != null) {
                            writeDouble(dos, toWrite);
                        } else {
                            writeDouble(dos, 0);
                        }
                    } else if (types[i] == int[].class) {
                        int[] toWrite = (int[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < lengths[i]; j++) {
                                writeInt(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            writeInt(dos, 0);
                        }
                    } else if (types[i] == short[].class) {
                        short[] toWrite = (short[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < lengths[i]; j++) {
                                writeShort(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            writeShort(dos, (short) 0);
                        }
                    } else if (types[i] == long[].class) {
                        long[] toWrite = (long[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < lengths[i]; j++) {
                                writeLong(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            writeLong(dos, 0);
                        }
                    } else if (types[i] == byte[].class) {
                        byte[] toWrite = (byte[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < max; j++) {
                                writeByte(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            // writeByte(dos, '\0');
                            writeByte(dos, (byte) 0);
                        }
                    } else if (types[i] == float[].class) {
                        float[] toWrite = (float[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < lengths[i]; j++) {
                                writeFloat(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            writeFloat(dos, 0);
                        }
                    } else if (types[i] == double[].class) {
                        double[] toWrite = (double[]) structure.get(keys[i]);
                        int max = 0;
                        if (toWrite != null) {
                            max = Math.min(lengths[i], toWrite.length);
                            for (int j = 0; j < lengths[i]; j++) {
                                writeDouble(dos, toWrite[j]);
                            }
                        }
                        for (int j = max; j < lengths[i]; j++) {
                            writeDouble(dos, 0);
                        }
                    } else if (types[i] == byte[][].class) {
                        byte[][] toWrite = (byte[][]) structure.get(keys[i]);
                        if (toWrite != null) {
                            for (int k = 0; k < counts[i]; k++) {
                                for (int j = 0; j < lengths[i]; j++) {
                                    writeByte(dos, toWrite[k][j]);
                                }
                            }
                        } else {
                            for (int j = 0; j < counts[i] * lengths[i]; j++) {
                                writeByte(dos, (byte) 0);
                            }
                        }
                    } else {
                        if (TransactionImpl.current() != null) {
                            try {
                                TransactionImpl.current().rollback_only();
                            } catch (TransactionException e) {
                                throw new ConnectionException(ConnectionImpl.TPESYSTEM,
                                        "Could not mark transaction for rollback only");
                            }
                        }
                        throw new ConnectionException(ConnectionImpl.TPEOTYPE, "Could not serialize: " + types[i]);
                    }
                } catch (IOException e) {
                    throw new ConnectionException(ConnectionImpl.TPEOTYPE, "Could not parse the value as: " + keys[i]
                            + " was not a " + types[i] + " and even if it was an array of that type its length was not: "
                            + lengths[i]);
                }
            }
            toReturn = baos.toByteArray();
        } else {
            toReturn = getRawData();
        }
        if (toReturn == null) {
            toReturn = new byte[1];
        }
        return toReturn;
    }

    /**
     * Write a byte during serialization/
     * 
     * @param dos The output stream to write to.
     * @param b The byte to write.
     * @throws IOException In case the output stream fails.
     */
    private void writeByte(DataOutputStream dos, byte b) throws IOException {
        dos.writeByte(b);

        currentPos += 1;
    }

    /**
     * Read a byte during deserialization.
     * 
     * @param dis The input stream to read from
     * @return The byte
     * @throws IOException In case the stream cannot be read.
     */
    private byte readByte(DataInputStream dis) throws IOException {
        currentPos += 1;

        byte x = dis.readByte();
        ByteBuffer bbuf = ByteBuffer.allocate(BYTE_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.put(x);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        return bbuf.get(0);
    }

    private void writeLong(DataOutputStream dos, long x) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(LONG_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putLong(x);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        //long toWrite = bbuf.getLong(0);    
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        
        byte[] data = new byte[8];
        Arrays.fill(data, (byte)0);
        System.arraycopy(bbuf.array(), 4, data, 0, 4);
        
        //dos.write(bbuf.array(), 4, 4);
        dos.write(data);
        currentPos += LONG_SIZE;
    }

    private long readLong(DataInputStream dis) throws IOException {
        currentPos += LONG_SIZE;
        //long x = dis.readLong();
         
        ByteBuffer bbuf = ByteBuffer.allocate(LONG_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        byte[] data = new byte[8];
        Arrays.fill(data, (byte)0);
        
        dis.read(data, 4, 4);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.put(data);
        long x = bbuf.getLong(0);
        //read the next 4 bytes
        dis.read(data, 0, 4);
        return x;
    }

    private void writeInt(DataOutputStream dos, int x) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(INT_SIZE);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.putInt(x);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        int toWrite = bbuf.getInt(0);
        dos.writeInt(toWrite);
        currentPos += INT_SIZE;
    }

    private int readInt(DataInputStream dis) throws IOException {
        currentPos += INT_SIZE;
        int x = dis.readInt();
        ByteBuffer bbuf = ByteBuffer.allocate(INT_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putInt(x);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        return bbuf.getInt(0);
    }

    private void writeShort(DataOutputStream dos, short x) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(SHORT_SIZE);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.putShort(x);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        short toWrite = bbuf.getShort(0);
        dos.writeShort(toWrite);

        currentPos += SHORT_SIZE;
    }

    private short readShort(DataInputStream dis) throws IOException {
        currentPos += SHORT_SIZE;
        short x = dis.readShort();
        ByteBuffer bbuf = ByteBuffer.allocate(SHORT_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putShort(x);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        return bbuf.getShort(0);
    }

    private void writeFloat(DataOutputStream dos, float x) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(FLOAT_SIZE);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.putFloat(x);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        float toWrite = bbuf.getFloat(0);
        dos.writeFloat(toWrite);

        currentPos += FLOAT_SIZE;
    }

    private float readFloat(DataInputStream dis) throws IOException {
        currentPos += FLOAT_SIZE;
        float x = dis.readFloat();
        ByteBuffer bbuf = ByteBuffer.allocate(FLOAT_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putFloat(x);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        return bbuf.getFloat(0);
    }

    private void writeDouble(DataOutputStream dos, double x) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(DOUBLE_SIZE);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.putDouble(x);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        double toWrite = bbuf.getDouble(0);
        dos.writeDouble(toWrite);

        currentPos += DOUBLE_SIZE;
    }

    private double readDouble(DataInputStream dis) throws IOException {
        currentPos += DOUBLE_SIZE;
        double x = dis.readDouble();
        ByteBuffer bbuf = ByteBuffer.allocate(DOUBLE_SIZE);
        //bbuf.order(ByteOrder.LITTLE_ENDIAN);
        bbuf.putDouble(x);
        //bbuf.order(ByteOrder.BIG_ENDIAN);
        return bbuf.getDouble(0);
    }

    /**
     * Get the type
     * 
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the subtype
     * 
     * @return The subtype
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Clear the content of the buffer
     */
    public void clear() {
        structure.clear();
        data = null;
    }

    /**
     * Get the value of an attribute.
     * 
     * @param key The key
     * @param type The type
     * @return The value
     * @throws ConnectionException In case the message is not formatted yet.
     */
    protected Object getAttributeValue(String key, Class type) throws ConnectionException {
        if (!formatted) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO, "Message not formatted");
        }
        int position = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                position = i;
            }
        }
        if (position == -1) {
            throw new ConnectionException(ConnectionImpl.TPEITYPE, "Key is not part of the structure: " + key);
        } else if (types[position] != type) {
            throw new ConnectionException(ConnectionImpl.TPEITYPE, "Key is not request type, it is a: " + types[position]);

        }
        return structure.get(key);
    }

    /**
     * Set the value.
     * 
     * @param key The key to set
     * @param type The type of the value.
     * @param value The value to use
     * @throws ConnectionException In case the message is not formatted.
     */
    protected void setAttributeValue(String key, Class type, Object value) throws ConnectionException {
        if (!formatted) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO, "Message not formatted");
        }
        int position = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                position = i;
            }
        }
        if (position == -1) {
            throw new ConnectionException(ConnectionImpl.TPEITYPE, "Key is not part of the structure: " + key);
        } else if (types[position] != type) {
            throw new ConnectionException(ConnectionImpl.TPEITYPE, "Key is not request type, it is a: " + types[position]);

        }
        structure.put(key, value);
    }

    /**
     * Set the raw data, used by the X_OCTET buffer.
     * 
     * @param bytes The data to use.
     */
    protected void setRawData(byte[] bytes) {
        this.data = bytes;
    }

    /**
     * Get the raw data, used internally and by the X_OCTET buffer.
     * 
     * @return The data.
     */
    protected byte[] getRawData() {
        return data;
    }

    public int getLen() {
        return len;
    }
}
