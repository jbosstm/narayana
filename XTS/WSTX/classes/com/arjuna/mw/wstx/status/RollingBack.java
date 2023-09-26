/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wstx.status;

import com.arjuna.mw.wsas.status.Status;

/**
 * The transaction is rolling back.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RollingBack.java,v 1.1 2002/11/25 11:00:53 nmcl Exp $
 * @since 1.0.
 */

public class RollingBack implements Status
{

    public static RollingBack instance ()
    {
	return _instance;
    }
    
    public String toString ()
    {
	return "Status.RollingBack";
    }

    private RollingBack ()
    {
    }

    private static final RollingBack _instance = new RollingBack();
    
}