/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.orbportability.internal.orbspecific.javaidl.oa.implementations;

import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;

import com.arjuna.orbportability.internal.orbspecific.oa.implementations.POABase;
import com.arjuna.orbportability.logging.opLogger;

public class javaidl_1_4 extends POABase
{
    /**
     * Create a child POA of the root POA.
     */
    public void createPOA(String adapterName,
			  Policy[] policies) throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive, SystemException
    {
	if (_poa == null)
	{
        opLogger.i18NLogger.warn_internal_orbspecific_oa_implementations("javaidl_1_4.createPOA");

	    throw new AdapterInactive();
	}

	POA childPoa = _poa.create_POA(adapterName, _poa.the_POAManager(), policies);

	childPoa.the_POAManager().activate();

	super._poas.put(adapterName, childPoa);
    }
}