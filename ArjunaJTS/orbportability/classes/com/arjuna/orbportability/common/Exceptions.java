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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Exceptions.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.common;

/**
 * Some exception values we may set.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Exceptions.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Exceptions
{

public static final int BAD_OPERATION_BASE = 100000;
public static final int NOT_FOUND = BAD_OPERATION_BASE+1;
public static final int CANNOT_PROCEED = BAD_OPERATION_BASE+2;
public static final int INVALID_NAME = BAD_OPERATION_BASE+3;

/*
 * We throw this BAD_PARAM when narrow fails.
 */

public static final int BAD_OBJECT_REF = 10029;
    
}
