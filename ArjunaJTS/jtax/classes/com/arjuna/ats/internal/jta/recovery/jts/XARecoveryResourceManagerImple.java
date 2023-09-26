/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.recovery.jts;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.recovery.XARecoveryResourceManager;

public class XARecoveryResourceManagerImple implements XARecoveryResourceManager
{

    public XARecoveryResource getResource (Uid uid)
    {
	return new XARecoveryResourceImple(uid);
    }
    
    public XARecoveryResource getResource (Uid uid, XAResource res)
    {
	return new XARecoveryResourceImple(uid, res);
    }

    public String type ()
    {
	return XARecoveryResourceImple.typeName();
    }

}