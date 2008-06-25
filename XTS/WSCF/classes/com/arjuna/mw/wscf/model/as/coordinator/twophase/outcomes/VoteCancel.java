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
 * $Id: VoteCancel.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator.twophase.outcomes;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Failure;

import com.arjuna.mw.wsas.exceptions.SystemException;

import java.util.Arrays;

/**
 * The inferior votes that is has cancelled. The coordinator service
 * may inform the inferior of the final decision (hopefully to cancel
 * as well), but it need not.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: VoteCancel.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

public class VoteCancel implements Vote
{

    public VoteCancel ()
    {
	_quals = null;
    }
    
    public VoteCancel (Qualifier[] quals)
    {
	_quals = quals;
    }

    public boolean equals (Object o)
    {
	if (o == null)
	    return false;

	if (o instanceof VoteCancel)
	    return true;
	else
	    return false;
    }

    /**
     * @return a printable version of the vote.
     */

    public String toString ()
    {
	return "org.w3c.wscf.twophase.outcomes.VoteCancel";
    }

    public CompletionStatus completedStatus () throws SystemException
    {
	return Failure.instance();
    }
    
    public String name () throws SystemException
    {
	return toString();
    }

    public Object data () throws SystemException
    {
	return Arrays.asList(_quals);
    }
    
    private Qualifier[] _quals;
    
}
