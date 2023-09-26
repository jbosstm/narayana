/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wscf.tests;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.model.twophase.common.CoordinationResult;
import com.arjuna.mw.wscf.model.twophase.participants.Synchronization;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseSynchronization.java,v 1.1 2003/01/07 10:37:17 nmcl Exp $
 * @since 1.0.
 */

public class TwoPhaseSynchronization implements Synchronization
{

    public TwoPhaseSynchronization()
    {
    }

    public void beforeCompletion () throws SystemException
    {
	System.out.println("TwoPhaseSynchronization.beforeCompletion");
    }

    public void afterCompletion (int status) throws SystemException
    {
	System.out.println("TwoPhaseSynchronization.afterCompletion ( "+CoordinationResult.stringForm(status)+" )");
    }

}