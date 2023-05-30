/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.mw.wst11;


public class BusinessActivityManagerFactory
{
    public static BusinessActivityManager businessActivityManager ()
    {
        return BusinessActivityManager.getBusinessActivityManager() ;
	}
}