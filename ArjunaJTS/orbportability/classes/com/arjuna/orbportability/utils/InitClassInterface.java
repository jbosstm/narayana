/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */




package com.arjuna.orbportability.utils;

/**
 * This interface can be implemented by classes which have been registered as pre/post-initialisation
 * classes.  If this interface is used then the object which has been/is being intialised will be passed
 * to it via the setAssociatedObject method.
 *
 * @author Richard A. Begg (richard_begg@hp.com)
 * @version $Id:%
 */
public interface InitClassInterface
{
    /**
     * This method is called and passed the object which is associated with this pre/post-initialisation routine.
     *
     * @param obj The object which has or is being initialised.
     */
    public void invoke(Object obj);
}