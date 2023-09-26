/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wst11;

public class UserBusinessActivityFactory
{
    public static UserBusinessActivity userBusinessActivity ()
    {
        return UserBusinessActivity.getUserBusinessActivity() ;
	}

    public static UserBusinessActivity userSubordinateBusinessActivity ()
    {
        return UserBusinessActivity.getUserBusinessActivity().getUserSubordinateBusinessActivity() ;
    }
}