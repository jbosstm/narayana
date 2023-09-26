/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation.postinit;

import com.arjuna.orbportability.utils.InitClassInterface;

public class PostInitialisationUsingInterface implements InitClassInterface
{
    protected static Object    _obj = null;

    public final static Object getObject()
    {
        return _obj;
    }

    public final static void setObject(Object obj)
    {
        _obj = obj;
    }

    public PostInitialisationUsingInterface()
    {
        System.out.println("Created 'PostInitialisationUsingInterface'");
    }

    /**
     * This method is called and passed the object which is associated with this pre/post-initialisation routine.
     *
     * @param obj The object which has or is being initialised.
     */
    public void invoke(Object obj)
    {
        System.out.println("PostInitialisationUsingInterface called passing obj="+obj);
        PostInitialisationUsingInterface.setObject(obj);
    }
}