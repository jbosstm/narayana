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
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: InputBuffer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.state;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.*;

import java.io.IOException;

/**
 * An InputBuffer is used to retrieve various Java types from a byte
 * stream created using an OutputBuffer. Similar to java serialization.
 * However, InputBuffers are compatible with OTSArjuna states.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: InputBuffer.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.state.InputBuffer_1 com.arjuna.ats.arjuna.state.InputBuffer_1 - Invalid input buffer: byte.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_2 com.arjuna.ats.arjuna.state.InputBuffer_2 - Invalid input buffer: bytes.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_3 com.arjuna.ats.arjuna.state.InputBuffer_3 - Invalid input buffer: boolean.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_4 com.arjuna.ats.arjuna.state.InputBuffer_4 - Invalid input buffer: char.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_5 com.arjuna.ats.arjuna.state.InputBuffer_5 - Invalid input buffer: short.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_6 com.arjuna.ats.arjuna.state.InputBuffer_6 - Invalid input buffer: int.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_7 com.arjuna.ats.arjuna.state.InputBuffer_7 - Invalid input buffer: long.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_8 com.arjuna.ats.arjuna.state.InputBuffer_8 - Invalid input buffer: float.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_9 com.arjuna.ats.arjuna.state.InputBuffer_9 - Invalid input buffer: double
 * @message com.arjuna.ats.arjuna.state.InputBuffer_10 com.arjuna.ats.arjuna.state.InputBuffer_10 - Invalid input buffer: string.
 * @message com.arjuna.ats.arjuna.state.InputBuffer_11 com.arjuna.ats.arjuna.state.InputBuffer_11 - Invalid from buffer
 */

public class InputBuffer
{

    /**
     * Create a new buffer.
     */

public InputBuffer ()
    {
	_byteArray = null;
	_valid = false;
	_inputStream = null;
	_input = null;
    }

    /**
     * Create our own copy of the byte array.
     */
    
public InputBuffer (byte b[])
    {
	_valid = true;

	_byteArray = new byte[b.length];

	System.arraycopy(b, 0, _byteArray, 0, b.length);
	
	try
	{
	    _inputStream = new ByteArrayInputStream(_byteArray);
	    _input = new DataInputStream(_inputStream);

	    skipHeader();
	}
	catch (IOException e)
	{
	    _valid = false;
	}
    }

    /**
     * Create a new buffer and copy the provided one.
     */

public InputBuffer (InputBuffer buff)
    {
	_byteArray = null;
	_valid = false;
	_inputStream = null;
	_input = null;
	
	copy(buff);
    }

    /**
     * Is the buffer valid?
     */

public final synchronized boolean valid ()
    {
	return _valid;
    }

    /**
     * Copy the existing buffer.
     */

public synchronized void copy (InputBuffer buff)
    {
	if (buff._valid)
	{
	    _byteArray = new byte[buff._byteArray.length];
	    _valid = true;

	    System.arraycopy(buff._byteArray, 0, _byteArray, 0, buff._byteArray.length);	    

	    try
	    {
		_inputStream = new ByteArrayInputStream(_byteArray);
		_input = new DataInputStream(_inputStream);

		skipHeader();
	    }
	    catch (IOException e)
	    {
		_valid = false;
	    }
	}
    }

    /**
     * Return the length of the byte buffer.
     */

public final synchronized int length () 
    {
	return ((_byteArray == null) ? 0 : _byteArray.length);
    }

    /**
     * Return the internal byte buffer.
     */

public final synchronized byte[] buffer ()
    {
	return _byteArray;
    }

    /**
     * Set the buffer to be used by this instance.
     */

public final synchronized void setBuffer (byte[] b)
    {
	_byteArray = new byte[b.length];

	System.arraycopy(b, 0, _byteArray, 0, b.length);
	
	try
	{
	    _inputStream = new ByteArrayInputStream(_byteArray);
	    _input = new DataInputStream(_inputStream);

	    _valid = true;

	    skipHeader();
	}
	catch (Exception e)
	{
	    _byteArray = null;
	    _valid = false;
	}
    }

    /**
     * Unpack a byte from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized byte unpackByte () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_1"));
	
	int i = unpackInt();

	return (byte) i;
    }

    /**
     * Unpack the next byte array from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized byte[] unpackBytes () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_2"));

	int index = unpackInt();
	int size = unpackInt();
	byte b[] = new byte[size];

	if (size > 0)
	{
	    _input.readFully(b);

	    realign(size);
	}
	
	return b;
    }

    /**
     * Unpack a boolean from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized boolean unpackBoolean () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_3"));

	_valid = false;
	
	_inputStream.skip(3);

	boolean b = _input.readBoolean();

	_valid = true;

	return b;
    }

    /**
     * Unpack a character from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized char unpackChar () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_4"));
	
	int i = unpackInt();
	
	return (char) i;
    }

    /**
     * Unpack a short from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized short unpackShort () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_5"));
	
	int i = unpackInt();

	return (short) i;
    }    

    /**
     * Unpack an integer from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized int unpackInt () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_6"));

	_valid = false;

	int i = _input.readInt();

	_valid = true;

	return i;
    }

    /**
     * Unpack a long from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized long unpackLong () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_7"));

	_valid = false;

	long l = _input.readLong();

	_valid = true;

	return l;
    }

    /**
     * Unpack a float from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized float unpackFloat () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_8"));

	_valid = false;

	float f = _input.readFloat();

	_valid = true;

	return f;
    }

    /**
     * Unpack a double from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public final synchronized double unpackDouble () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_9"));

	_valid = false;

	double d = _input.readDouble();

	_valid = true;

	return d;
    }

    /**
     * Unpack a String from the stream.
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     *
     * Currently different from the C++ version in that
     * a distinct new instance will always be returned, rather
     * than a reference to a previously returned object in the
     * case of the "same" string.
     */
    
public final synchronized String unpackString () throws IOException
    {
	if (!_valid)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_10"));
	
	int index = unpackInt();
	int length = unpackInt();

	if (length == 0)
	    return null;
	
	/*
	 * We don't need the '\0' character which Arjuna puts in
	 * the buffer. We only put it in for compatibility with
	 * C++. So ignore it.
	 */
	
	byte[] b = new byte[length-1];
	byte[] dummy = new byte[1];
	
	_input.read(b, 0, length-1);
	_input.read(dummy, 0, 1);

	realign(length);

	return new String(b);
    }

    /**
     * Unpack a buffer from the provided buffer, and
     * initialise this instance with it.
     *
     * If the next item in the buffer is not of the right
     * type then an IOException is thrown.
     */

public synchronized void unpackFrom (InputBuffer buff) throws IOException
    {
	if (buff == null)
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.state.InputBuffer_11"));
	
	_valid = false;
	
	/*
	 * unpack number of bytes, then create new byte array
	 * and unpack each byte separately.
	 */

	int i = buff.unpackInt();

	_byteArray = new byte[i];

	for (int j = 0; j < i; j++)
	    _byteArray[j] = buff.unpackByte();

	_valid = true;
	    
	try
	{
	    _inputStream = new ByteArrayInputStream(_byteArray);
	    _input = new DataInputStream(_inputStream);

	    skipHeader();
	}
	catch (IOException e)
	{
	    _valid = false;
	}
    }

    /**
     * Reset the read pointer for this buffer.
     */

public final boolean reread ()
    {
	if (!_valid)
	    return false;
	
	try
	{
	    _inputStream = new ByteArrayInputStream(_byteArray);
	    _input = new DataInputStream(_inputStream);

	    skipHeader();
	}
	catch (IOException e)
	{
	    _valid = false;
	}

	return _valid;
    }

    /**
     * Print information about this instance.
     */

public void print (PrintWriter strm)
    {
	if (_valid)
	{
	    strm.println("InputBuffer : \n");

	    strm.println("InputBuffer : \n");

	    for (int i = 0; i < _byteArray.length; i++)
		strm.write((char) _byteArray[i]);
	}
	else
	    strm.println("InputBuffer : invalid.");
    }

private final void realign (int amount) throws IOException
    {
	if ((amount % OutputBuffer.ALIGNMENT) > 0)
	{
	    int excess = OutputBuffer.ALIGNMENT - (amount % OutputBuffer.ALIGNMENT);

	    if (_inputStream.available() < excess)
		excess = _inputStream.available();
	
	    _input.skipBytes(excess);
	}
    }
    
private final void skipHeader () throws IOException
    {
	_inputStream.skip(OutputBuffer.headerSize);  // sizeof buffer header
    }

protected boolean _valid;
    
private DataInputStream      _input;
private ByteArrayInputStream _inputStream;
private byte[]               _byteArray;    
    
}
