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
 * $Id: ClassName.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import java.io.IOException;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Each implementation type may provide an instance of this class
 * to uniquely identify itself to its users and the Inventory.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ClassName.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ClassName
{

public ClassName ()
    {
	this.copy(ClassName.invalid());
    }
    
public ClassName (String className)
    {
	_className = null;
    
	if (className != null)
	    _className = new String(className);
	else
	    this.copy(ClassName.invalid());
    }
    
public ClassName (ClassName name)
    {
	_className = null;

	this.copy(name);
    }

public static ClassName invalid()
    {
	return _invalid;
    }

public String stringForm ()
    {
	return _className;
    }

public void pack (OutputBuffer buff) throws IOException
    {
	buff.packString(_className);
    }

public void unpack (InputBuffer buff) throws IOException
    {
	_className = buff.unpackString();
    }
    
    /** 
     * @message com.arjuna.ats.arjuna.gandiva.ClassName_1 [com.arjuna.ats.arjuna.gandiva.ClassName_1] - ClassName.copy - no class name available!
     */

public void copy (ClassName className)
    {
	if (this == className)
	    return;
	
	if (className._className != null)
	    _className = new String(className._className);
	else
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.gandiva.ClassName_1");
	}
    }

public boolean equals (ClassName className)
    {
	if ((className == null) && (_className == null))
	    return false;
	
	if (className == null)
	    return false;
	
	if (_className != className._className)
	    return (_className.compareTo(className._className) == 0);
	else
	    return true;
    }

public boolean notEquals (ClassName className)
    {
	if (_className == className._className)
	    return false;
	else
	    return (_className.compareTo(className._className) != 0);
    }

public String toString ()
    {
	return new String("<ClassName:"+_className+">");
    }

private String _className;
    
private static final ClassName _invalid = new ClassName("$Invalid");

}
