/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.participants;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * Business Activities don't expose synchronizations but they are used internally to ensure
 * that the transcation association is cleaned up and this interface is used to define the
 * behaviour of the synchronization.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Synchronization.java,v 1.2 2005/05/19 12:13:26 nmcl Exp $
 * @since 1.0.
 */

public interface Synchronization
{

    /**
     * The transaction that the instance is enrolled with has either closed or compensated and
     * the relevant termination state ss passed as a parameter.
     *
     * @param status The state in which the transaction completed.
     *
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any error occurs. This has no
     * affect on the outcome of the transaction.
     */

    public void afterCompletion (int status) throws SystemException;

}