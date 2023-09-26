/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.interposition;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

/**
 * Different transaction factories can be added dynamically
 * to the system to deal with specific interposition types.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FactoryCreator.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public interface FactoryCreator
{

    public ControlImple recreateLocal (PropagationContext ctx) throws SystemException;

    public Control recreate (PropagationContext ctx) throws SystemException;
 
};