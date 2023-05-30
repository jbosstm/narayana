/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import javax.transaction.xa.XAResource;

public interface XACreator
{
    
    public XAResource create (String param, boolean print);

}