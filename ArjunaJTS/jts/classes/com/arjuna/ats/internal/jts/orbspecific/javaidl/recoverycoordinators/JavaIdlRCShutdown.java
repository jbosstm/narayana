/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.orbportability.oa.PreShutdown;

public class JavaIdlRCShutdown extends PreShutdown
{

    public JavaIdlRCShutdown()
    {
        super("JavaIdlRCShutdown");
    }

    public void work ()
    {
        JavaIdlRCServiceInit.shutdownRCService();
    }

}