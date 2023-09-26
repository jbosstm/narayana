/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.interposition.resources.restricted;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.internal.jts.interposition.FactoryCreator;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

public class RestrictedInterpositionCreator implements FactoryCreator
{

public ControlImple recreateLocal (PropagationContext ctx) throws SystemException
    {
	return RestrictedInterposition.create(ctx);
    }

public Control recreate (PropagationContext ctx) throws SystemException
    {
	return recreateLocal(ctx).getControl();
    }

};