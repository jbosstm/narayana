/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.common;

/**
 * Runtime configuration information for the interceptors.
 *
 * @author Kevin Conner
 */

public class InterceptorInfo
{
    private static final boolean OTS_NEED_TRAN_CONTEXT = jtsPropertyManager.getJTSEnvironmentBean().isNeedTranContext();
    private static final boolean OTS_ALWAYS_PROPAGATE = jtsPropertyManager.getJTSEnvironmentBean().isAlwaysPropagateContext();

    /**
     * Get the flag indicating whether a transaction context is required.
     * @return true if a context is required, false otherwise.
     */
    public static boolean getNeedTranContext()
    {
	return OTS_NEED_TRAN_CONTEXT ;
    }
    
    /**
     * Get the flag indicating whether a transaction context should always be propagated.
     * @return true if a context is alwats propagated, false if it is only sent to OTS transactional objects.
     */
    public static boolean getAlwaysPropagate()
    {
	return OTS_ALWAYS_PROPAGATE ;
    }
}