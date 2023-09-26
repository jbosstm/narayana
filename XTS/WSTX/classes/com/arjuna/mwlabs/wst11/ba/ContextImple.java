/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.ba;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.mw.wsc11.context.Context;


public class ContextImple implements Context
{
    public ContextImple(CoordinationContextType ctx)
    {
        _coordContext = ctx;
    }

    public boolean equals (Object obj)
    {
        if (obj instanceof ContextImple)
        {
    	    ContextImple ci = (ContextImple) obj;

            return ci.getCoordinationContext().getIdentifier().getValue().equals(_coordContext.getIdentifier().getValue());
        }
        else
            return false;
    }

    public CoordinationContextType getCoordinationContext ()
    {
    	return _coordContext;
    }

    public void setCoordinationContext (CoordinationContextType cc)
    {
    	_coordContext = cc;
    }

    public String toString ()
    {
    	return "BusinessActivityIdentifier: "+_coordContext.getIdentifier().getValue();
    }

    private CoordinationContextType _coordContext;
}