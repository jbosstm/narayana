/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
package com.arjuna.ats.jbossatx.jta;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.jbossatx.BaseTransactionManagerDelegate;

public class TransactionManagerDelegate extends BaseTransactionManagerDelegate implements ObjectFactory
{
    /**
     * Construct the delegate with the appropriate transaction manager
     */
    public TransactionManagerDelegate()
    {
        super(new TransactionManagerImple());
    }
    
    /**
     * Get the transaction manager from the factory.
     * @param initObj The initialisation object.
     * @param relativeName The instance name relative to the context.
     * @param namingContext The naming context for the instance.
     * @param env The environment.
     */
    public Object getObjectInstance(final Object initObj,
           final Name relativeName, final Context namingContext,
           final Hashtable env)
        throws Exception
    {
        return this ;
    }
}
