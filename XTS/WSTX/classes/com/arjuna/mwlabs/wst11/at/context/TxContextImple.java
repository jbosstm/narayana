/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.at.context;

import com.arjuna.mw.wsc11.context.Context;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mwlabs.wst11.at.ContextImple;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TxContextImple.java,v 1.8.4.1 2005/11/22 10:36:15 kconner Exp $
 */

public class TxContextImple implements TxContext
{

	public TxContextImple(CoordinationContextType ctx)
	{
		_context = new ContextImple(ctx);
	}

	public TxContextImple(Context context)
	{
		_context = context;
	}

	public boolean valid ()
	{
		return (_context != null) ;
	}

	public boolean equals (Object obj)
	{
		if (obj instanceof TxContextImple)
		{
			TxContextImple compare = (TxContextImple) obj;

			return compare.context().equals(_context);
		}
		else
			return false;
	}

	public final String identifier ()
	{
	    final String value = _context.getCoordinationContext().getIdentifier().getValue();
	    if ((value != null) && value.startsWith("urn:"))
        {
            return value.substring(4) ;
        }
	    return value ;
	}

	public final Context context ()
	{
		return _context;
	}

	public String toString ()
	{
		return _context.toString();
	}

    public boolean isSecure()
    {
        if (valid()) {
            CoordinationContextType coordinationContextType = _context.getCoordinationContext();
            W3CEndpointReference epref = coordinationContextType.getRegistrationService();
            NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, epref);
            String address = nativeRef.getAddress();
            return address.startsWith("https");
        }
        return false;
    }

	private Context _context;

}