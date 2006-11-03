/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: IORRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.recovery;

import com.arjuna.orbportability.ORB;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.omg.CORBA.SystemException;

/**
 * Implementation specific mechanisms for recovering an IOR, i.e.,
 * making an IOR that represents a failed object valid again.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: IORRecovery.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public interface IORRecovery
{

public org.omg.CORBA.Object recover (ORB orb, String name, org.omg.CORBA.Object obj,
				     Object[] params) throws SystemException;

};
