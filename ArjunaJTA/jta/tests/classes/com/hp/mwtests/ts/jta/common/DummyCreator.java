/*
 * SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAResource;

public class DummyCreator implements XACreator
{

    public XAResource create (String param, boolean print)
    {
	return new DummyXA(print);
    }

}