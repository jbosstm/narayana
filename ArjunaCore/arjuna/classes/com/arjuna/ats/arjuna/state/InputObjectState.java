/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.state;

import java.io.IOException;
import java.io.PrintWriter;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * InputObjectState provides some additional methods to
 * a basic InputBuffer.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: InputObjectState.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class InputObjectState extends InputBuffer
{

public InputObjectState ()
    {
	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("InputObjectState::InputObjectState()");
    }
       	
	bufferUid = new Uid(Uid.nullUid());
	super._valid = false;
	imageType = null;
    }
    
public InputObjectState (InputObjectState copyFrom)
    {
	super(copyFrom);

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("InputObjectState::InputObjectState(" + copyFrom + ")");
    }
	
	try
	{
	    bufferUid = new Uid(copyFrom.bufferUid);
	    super._valid = bufferUid.valid();
	
	    imageType = new String((copyFrom.imageType == null) ? "" : copyFrom.imageType);
	}
	catch (Exception ex)
	{
	    super._valid = false;
	}
    }

public InputObjectState (OutputObjectState copyFrom)
    {
	super(copyFrom.buffer());

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("InputObjectState::InputObjectState(" + copyFrom + ")");
    }

	try
	{
	    bufferUid = new Uid(copyFrom.stateUid());
	    super._valid = super._valid && bufferUid.valid();
	
	    imageType = new String((copyFrom.type() == null) ? "" : copyFrom.type());
	}
	catch (Exception ex)
	{
	    super._valid = false;
	}
    }
    
public InputObjectState (Uid newUid, String tName, byte[] buff)
    {
	super(buff);  // implicitly copies the array contents.

	if (tsLogger.logger.isTraceEnabled()) {
        tsLogger.logger.trace("InputObjectState::InputObjectState(" + newUid + ", " + tName + ")");
    }

	bufferUid = new Uid(newUid);
	super._valid = super._valid && bufferUid.valid();
	
	imageType = new String(tName);
    }

public final boolean notempty ()
    {
	return ((length() > 0) ? true : false);
    }

public final int size () 
    {
	return (length());
    }

public final Uid stateUid ()
    {
	return bufferUid;
    }

public final String type ()
    {
	return imageType;
    }

public void copyFrom (OutputObjectState copyFrom)
    {
	super.setBuffer(copyFrom.buffer());

	bufferUid = new Uid(copyFrom.stateUid());
	super._valid = bufferUid.valid();
	
	imageType = (copyFrom.type() == null ? null : new String(copyFrom.type()));
    }
    
public void print (PrintWriter strm)
    {
	strm.println("InputObjectState Uid   : "+bufferUid+"\n");

	if (imageType != null)
	    strm.println("InputObjectState Type  : "+imageType+"\n");
	else
	    strm.println("InputObjectState Type  : null\n");

	strm.println("InputObjectState Size  : "+size()+"\n");
	strm.println("InputObjectState Buffer: ");

	super.print(strm);
    }

    public String toString ()
    {    
	String val = "InputObjectState Uid   : "+bufferUid+"\n";

	if (imageType != null)
	    val += "InputObjectState Type  : "+imageType+"\n";
	else
	    val += "InputObjectState Type  : null\n";

	val += "InputObjectState Size  : "+size()+"\n";
	val += "InputObjectState Buffer: ";

	return val;
    }

public synchronized void copy (InputObjectState objstate)
    {
        if (tsLogger.logger.isTraceEnabled())
            tsLogger.logger.trace("InputObjectState::copy for " + bufferUid);
       
	super.copy(objstate);

	bufferUid = new Uid(objstate.bufferUid);
	super._valid = bufferUid.valid();
	
	imageType = (objstate.imageType == null ? null : new String(objstate.imageType));
    }

public synchronized void unpackFrom (InputBuffer buff) throws IOException
    {
	imageType = buff.unpackString();
	
	bufferUid = UidHelper.unpackFrom(buff);

	super.unpackFrom(buff);
    }

private Uid    bufferUid;
private String imageType;

}