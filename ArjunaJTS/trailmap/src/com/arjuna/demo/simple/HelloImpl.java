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
 * Copyright (C) 2003, 2004
 * Arjuna Technologies Limited
 * Newcastle upon Tyne, UK
 *
 * $Id: HelloImpl.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.simple;


/**
 * This class is created and exposed as a remote object by the <CODE>HelloServer</CODE>. It is a CORBA object that
 * provides transaction-aware operations, in this case a single operation print_hello().
 *
 * If the print_hello command is issued within the scope of a transaction then the call will be executed within a
 * transaction.
 */
public class HelloImpl extends HelloPOA
{
    /**
     * This method simply displays a greeting on the console.
     */
    public void print_hello()
    {
        System.out.println("Hello - called within a scope of a transaction");
    }
}

