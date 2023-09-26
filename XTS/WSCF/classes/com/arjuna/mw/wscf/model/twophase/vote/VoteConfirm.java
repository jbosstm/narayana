/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.vote;

/**
 * The inferior votes that is can confirm. The coordinator service
 * should ultimately inform the participant of the final outcome.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: VoteConfirm.java,v 1.1 2003/01/07 10:33:47 nmcl Exp $
 */

public class VoteConfirm implements Vote
{

    public VoteConfirm ()
    {
    }

    public boolean equals (Object o)
    {
	if (o == null)
	    return false;

	if (o instanceof VoteConfirm)
	    return true;
	else
	    return false;
    }

    /**
     * @return a printable version of the vote.
     */

    public String toString ()
    {
	return "org.w3c.wscf.twophase.vote.VoteConfirm";
    }
    
}