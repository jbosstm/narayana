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
 * $Id: InputObjectState.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.state;

import com.arjuna.ats.arjuna.common.*;
import java.io.PrintWriter;

import com.arjuna.ats.arjuna.logging.*;

import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

import java.io.IOException;

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
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_BUFFER_MAN, "InputObjectState::InputObjectState()");
	}
       	
	bufferUid = new Uid(Uid.nullUid());
	super._valid = false;
	imageType = null;
    }
    
public InputObjectState (InputObjectState copyFrom)
    {
	super(copyFrom);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_BUFFER_MAN, "InputObjectState::InputObjectState("+copyFrom+")");
	}
	
	try
	{
	    bufferUid = new Uid(copyFrom.bufferUid);
	    super._valid = bufferUid.valid();
	
	    imageType = new String(copyFrom.imageType);
	}
	catch (Exception ex)
	{
	    super._valid = false;
	}
    }

public InputObjectState (OutputObjectState copyFrom)
    {
	super(copyFrom.buffer());

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_BUFFER_MAN, "InputObjectState::InputObjectState("+copyFrom+")");
	}

	try
	{
	    bufferUid = new Uid(copyFrom.stateUid());
	    super._valid = bufferUid.valid();
	
	    imageType = new String(copyFrom.type());
	}
	catch (Exception ex)
	{
	    super._valid = false;
	}
    }
    
public InputObjectState (Uid newUid, String tName, byte[] buff)
    {
	super(buff);  // implicitly copies the array contents.

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_BUFFER_MAN, 
				     "InputObjectState::InputObjectState("+newUid+", "+tName+")");
	}

	bufferUid = new Uid(newUid);
	super._valid = bufferUid.valid();
	
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
	
	imageType = new String(copyFrom.type());
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
	if (tsLogger.arjLogger.debugAllowed())
	    tsLogger.arjLogger.debug(DebugLevel.OPERATORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_BUFFER_MAN, "InputObjectState::copy for "+bufferUid);
       
	super.copy(objstate);

	bufferUid = new Uid(objstate.bufferUid);
	super._valid = bufferUid.valid();
	
	imageType = new String(objstate.imageType);
    }

public synchronized void unpackFrom (InputBuffer buff) throws IOException
    {
	imageType = buff.unpackString();
	bufferUid.unpack(buff);

	super.unpackFrom(buff);
    }

private Uid    bufferUid;
private String imageType;

}


