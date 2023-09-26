/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * An HLS may throw this error whenever a serious problem is encountered.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: HLSError.java,v 1.1 2002/11/25 10:51:42 nmcl Exp $
 * @since 1.0.
 */

public class HLSError extends Error
{

    public HLSError ()
    {
	super();
    }

    public HLSError (String s)
    {
	super(s);
    }

}