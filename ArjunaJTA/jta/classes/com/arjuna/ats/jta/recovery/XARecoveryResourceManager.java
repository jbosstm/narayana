/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.recovery;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.common.Uid;

public interface XARecoveryResourceManager
{

    public XARecoveryResource getResource (Uid uid);
    public XARecoveryResource getResource (Uid uid, XAResource res);

    public String type ();

}