/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations;

import com.arjuna.orbportability.internal.orbspecific.orb.implementations.ORBBase;

public class javaidl_1_4 extends ORBBase
{
    public javaidl_1_4()
    {
	System.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.Interceptors.PIORB");
	System.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");

    // it seems nothing ever reads this, so we should be able to get away without it
	// opPropertyManager.getPropertyManager().setProperty("com.arjuna.orbportability.internal.defaultBindMechanism", Services.bindString(Services.CONFIGURATION_FILE));
    }
}