/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * TestTransactionRolledBackExceptionCompletionCoordinatorParticipant.java
 */

package com.arjuna.wst11.tests;

import com.arjuna.wst11.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class TestTransactionRolledBackExceptionCompletionCoordinatorParticipant implements CompletionCoordinatorParticipant
{
    public TestTransactionRolledBackExceptionCompletionCoordinatorParticipant(W3CEndpointReference endpointReference)
    {
        this.endpointReference = endpointReference;
    }

    public void commit()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        throw new TransactionRolledBackException();
    }

    public void rollback()
        throws UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public W3CEndpointReference getParticipant() {
        return endpointReference;
    }

    private W3CEndpointReference endpointReference;
}