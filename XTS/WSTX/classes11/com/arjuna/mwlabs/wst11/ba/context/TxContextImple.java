/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TxContextImple.java,v 1.2.20.1 2005/11/22 10:36:16 kconner Exp $
 */

package com.arjuna.mwlabs.wst11.ba.context;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wsc11.context.Context;
import com.arjuna.mwlabs.wst11.ba.ContextImple;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TxContextImple.java,v 1.2.20.1 2005/11/22 10:36:16 kconner Exp $
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
            String address = coordinationContextType.getRegistrationService().getAddress();
            return address.startsWith("https");
        }
        return false;
    }

    private Context _context;

}