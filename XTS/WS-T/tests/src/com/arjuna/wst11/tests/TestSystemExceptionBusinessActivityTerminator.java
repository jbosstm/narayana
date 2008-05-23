/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: TestSystemExceptionBusinessActivityTerminator.java,v 1.3.20.1 2005/11/22 10:37:40 kconner Exp $
 */

package com.arjuna.wst11.tests;

import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class TestSystemExceptionBusinessActivityTerminator implements BusinessActivityTerminator
{
    public TestSystemExceptionBusinessActivityTerminator(W3CEndpointReference endpointReference)
    {
        this.endpointReference = endpointReference;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public void cancel ()
        throws UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public void complete ()
        throws UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public W3CEndpointReference getEndpoint() {
        return endpointReference;
    }

    private W3CEndpointReference endpointReference;
}