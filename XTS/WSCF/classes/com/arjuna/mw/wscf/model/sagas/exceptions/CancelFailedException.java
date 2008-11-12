/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2008,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2008
 *
 * $Id:$
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