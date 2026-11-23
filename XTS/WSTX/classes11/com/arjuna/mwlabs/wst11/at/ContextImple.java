package com.arjuna.mwlabs.wst11.at;

import com.arjuna.mw.wsc11.context.Context;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

/*
 * this is created and managed using a JaxWS coordination context but it will interconvert to an old style context
 * on demand, allowing interoperation between JaxWS based activator/ergistrator and the old style
 * BA/AT participant services
 */

public class ContextImple implements Context
{

	public ContextImple(final CoordinationContextType ctx)
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

	public String toString ()
	{
		// return DomUtil.nodeAsString(toXML());

		return "AtomicTransactionIdentifier: " + _coordContext.getIdentifier().getValue();
	}

    public CoordinationContextType getCoordinationContext()
    {
        return _coordContext;
    }

    public void setCoordinationContext(CoordinationContextType cc)
    {
        _coordContext = cc;
    }

    private CoordinationContextType _coordContext;
}
