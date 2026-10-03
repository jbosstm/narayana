package com.arjuna.mw.wsc11.context;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Context.java,v 1.1.24.1 2005/11/22 10:34:14 kconner Exp $
 * @since 1.0.
 */

public interface Context
{

    public CoordinationContextType getCoordinationContext ();

    public void setCoordinationContext (CoordinationContextType cc);
}
