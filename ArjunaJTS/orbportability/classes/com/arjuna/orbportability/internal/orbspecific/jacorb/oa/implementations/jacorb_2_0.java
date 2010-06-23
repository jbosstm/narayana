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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: jacorb_2_0.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.orbportability.internal.orbspecific.jacorb.oa.implementations;

import com.arjuna.orbportability.internal.orbspecific.oa.implementations.POABase;
import com.arjuna.orbportability.logging.opLogger;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POA;

public class jacorb_2_0 extends POABase
{
    public void createPOA (String adapterName, Policy[] policies)
            throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive,
            SystemException
    {
        if (_poa == null)
        {
            opLogger.i18NLogger.warn_internal_orbspecific_oa_implementations("jacorb_2_0.createPOA");

            throw new AdapterInactive();
        }

        POA childPoa = _poa.create_POA(adapterName, _poa.the_POAManager(),
                policies);

        childPoa.the_POAManager().activate();

        super._poas.put(adapterName, childPoa);
    }

}
