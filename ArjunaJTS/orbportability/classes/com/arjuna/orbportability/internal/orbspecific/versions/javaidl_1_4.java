/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.orbspecific.versions;

import com.arjuna.orbportability.ORBData;
import com.arjuna.orbportability.ORBType;

public class javaidl_1_4 implements ORBData
{
    public String getORBdata()
    {
        return "<orb-data><name>" + ORBType.javaidl +"</name><version><major>1</major><minor>3</minor></version><corba-version><major>2</major><minor>3</minor></corba-version></orb-data>";
    }
}