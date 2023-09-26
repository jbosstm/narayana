/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.context;

/**
 * The context is formed by the various HLSs that are present.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Context.java,v 1.2.24.1 2005/11/22 10:31:42 kconner Exp $
 * @since 1.0.
 */

public interface Context
{
    /**
     * @return the name of this context.
     */
    public String identifier() ;
}