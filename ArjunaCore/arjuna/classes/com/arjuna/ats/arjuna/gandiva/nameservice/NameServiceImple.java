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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: NameServiceImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva.nameservice;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import java.lang.Cloneable;

import java.io.IOException;

/**
 * NameService implementations are typically used by ObjectName to
 * store and retrieve object data in a manner specific to the NameService.
 * So, for example, one such implementation may use a database, whereas
 * another may use a remote property service. These details are hidden
 * by the implementation.
 *
 * All NameService implementations must derive from this base class and
 * provide implementations of the abstract methods.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NameServiceImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class NameServiceImple implements Cloneable
{

    /**
     * Return the type of the attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */
    
public abstract int attributeType (String objName, String attrName) throws IOException;

    /**
     * Return the first attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */

public abstract String firstAttributeName (String objName) throws IOException;

    /**
     * Return the next attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */

public abstract String nextAttributeName (String objName, String attrName) throws IOException;

    /**
     * Return the named long attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public abstract long getLongAttribute (String objName, String attrName) throws IOException;

    /**
     * Return the named String attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public abstract String getStringAttribute (String objName, String attrName) throws IOException;

    /**
     * Return the named ObjectName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public abstract ObjectName getObjectNameAttribute (String objName, String attrName) throws IOException;

    /**
     * Return the named ClassName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public abstract ClassName getClassNameAttribute (String objName, String attrName) throws IOException;

    /**
     * Return the named Uid attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public abstract Uid getUidAttribute (String objName, String attrName) throws IOException;

    /**
     * Set the named long attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public abstract String setLongAttribute (String objName, String attrName, long value) throws IOException;

    /**
     * Set the named String attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public abstract String setStringAttribute (String objName, String attrName, String value) throws IOException;

    /**
     * Set the named ObjectName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public abstract String setObjectNameAttribute (String objName, String attrName, ObjectName value) throws IOException;

    /**
     * Set the named ClassName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public abstract String setClassNameAttribute (String objName, String attrName, ClassName value) throws IOException;

    /**
     * Set the named Uid attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public abstract String setUidAttribute (String objName, String attrName, Uid value) throws IOException;

    /**
     * Remove the specified attribute and return a potentially modified
     * ObjectName identity.
     */

public abstract String removeAttribute (String objName, String attrName) throws IOException;

    /**
     * Return a unique (within the scope of this implementation) attribute
     * name.
     */

public abstract String uniqueAttributeName (String objName) throws IOException;

    /**
     * Return a unique (within the scope of this implementation) ObjectName,
     * which uses this NameService.
     */

public abstract ObjectName uniqueObjectName () throws IOException;

public abstract Object clone ();

public ClassName className ()
    {
	return NameServiceImple._className;
    }

public static ClassName name ()
    {
	return NameServiceImple._className;
    }
    
private static final ClassName _className = new ClassName("NameServiceImple");
    
}
