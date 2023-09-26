/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.vote;

/**
 * The inferior votes that it has done no work that requires to be involved
 * any further in the two-phase protocol. For example, it has not updated
 * any data. This can then be used by the coordinator to optimise the
 * subsequent phase of the protocol (if any).
 *
 * WARNING: this should be used with care.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: VoteReadOnly.java,v 1.1 2003/01/07 10:33:47 nmcl Exp $
 */

public class VoteReadOnly implements Vote
{

    public VoteReadOnly ()
    {
    }

    public boolean equals (Object o)
    {
	if (o == null)
	    return false;

	if (o instanceof VoteReadOnly)
	    return true;
	else
	    return false;
    }

    /**
     * @return a printable version of the vote.
     */

    public String toString ()
    {
	return "org.w3c.wscf.twophase.vote.VoteReadOnly";
    }
    
}