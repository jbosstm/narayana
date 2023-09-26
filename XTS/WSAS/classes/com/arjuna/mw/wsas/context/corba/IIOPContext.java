/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.context.corba;

import com.arjuna.mw.wsas.context.Context;

/**
 */

public interface IIOPContext extends Context
{

    /**
     * @return the position in the Service Context for this information.
     */

    public int position ();
    
}