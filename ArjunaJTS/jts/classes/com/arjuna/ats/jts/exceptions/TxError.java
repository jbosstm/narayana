/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.exceptions;


/**
 * Error that may be thrown during transaction processing.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TxError.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class TxError extends Error
{
    static final long serialVersionUID = -175416083725473793L;
    
    public TxError ()
    {
        super();
    }

    public TxError (String s)
    {
        super(s);
    }
}