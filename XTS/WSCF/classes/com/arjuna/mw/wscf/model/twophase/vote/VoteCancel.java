/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.vote;

/**
 * The inferior votes that is has cancelled. The coordinator service
 * may inform the inferior of the final decision (hopefully to cancel
 * as well), but it need not.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: VoteCancel.java,v 1.1 2003/01/07 10:33:47 nmcl Exp $
 */

public class VoteCancel implements Vote
{

    public VoteCancel ()
    {
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
	return "org.w3c.wscf.twophase.vote.VoteCancel";
    }

}