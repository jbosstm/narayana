/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wscf.model.sagas.exceptions;

import com.arjuna.mw.wsas.exceptions.SystemException;


/**
 * A fail occurred during a Business Agreement cancel operation -- only applies in WSBA 1.1.
 *
 * @author Andrew Dinn(adinn@redhat.com)
 * @version $Id:$
 */

public class CancelFailedException extends SystemException
{

    public CancelFailedException()
    {
	super();
    }

    public CancelFailedException(String s)
    {
	super(s);
    }

    public CancelFailedException(String s, int errorcode)
    {
	super(s, errorcode);
    }

}
