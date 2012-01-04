/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JNDIManager.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.jta.utils;

import javax.naming.ConfigurationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

public class JNDIManager
{
	/**
	 * Bind the underlying JTA implementations to the appropriate JNDI contexts.
     * @throws javax.naming.NamingException
	 */
	public static void bindJTAImplementations(InitialContext ctx) throws javax.naming.NamingException
	{
		bindJTATransactionManagerImplementation(ctx);
		bindJTAUserTransactionImplementation(ctx);
		bindJTATransactionSynchronizationRegistryImplementation(ctx);
	}

    /**
     * Bind the underlying JTA implementations to the appropriate JNDI contexts.
     * @throws javax.naming.NamingException
     */
    public static void bindJTAImplementation() throws javax.naming.NamingException
    {
        bindJTATransactionManagerImplementation();
        bindJTAUserTransactionImplementation();
		bindJTATransactionSynchronizationRegistryImplementation();
	}

	/**
     * Bind the currently configured transaction manager implementation to the default
     * JNDI context.
     * @throws javax.naming.NamingException
     */
    public static void bindJTATransactionManagerImplementation() throws javax.naming.NamingException
    {
        bindJTATransactionManagerImplementation(new InitialContext());
    }

    /**
     * Unbind the transaction manager from the default JNDI context.
     * @throws javax.naming.NamingException
     */
    public static void unbindJTATransactionManagerImplementation() throws javax.naming.NamingException
    {
        unbindJTATransactionManagerImplementation(new InitialContext());
    }

    /**
     * Bind the currently configured transaction manager implementation to the JNDI
     * context passed in.
     * @param initialContext
     * @throws javax.naming.NamingException
     */
	public static void bindJTATransactionManagerImplementation(InitialContext initialContext) throws javax.naming.NamingException
	{
        /** Look up and instantiate an instance of the configured transaction manager implementation **/
        String tmImplementation = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerClassName();

        /** Bind the transaction manager to the appropriate JNDI context **/
        Reference ref = new Reference(tmImplementation, tmImplementation, null);
        initialContext.rebind(getTransactionManagerJNDIName(), ref);
	}

    /**
     * Unbind the transaction manager from the provided JNDI context.
     * @param initialContext
     * @throws javax.naming.NamingException
     */
    public static void unbindJTATransactionManagerImplementation(InitialContext initialContext) throws javax.naming.NamingException
    {
        initialContext.unbind(getTransactionManagerJNDIName());
    }

    /**
     * Bind the currently configured user transaction implementation to the default JNDI
     * context.
     * @throws javax.naming.NamingException
     */
    public static void bindJTAUserTransactionImplementation() throws javax.naming.NamingException
    {
        bindJTAUserTransactionImplementation(new InitialContext());
    }

    /**
     * Bind the currently configured user transaction implementation to the passed in
     * JNDI context.
     * @param initialContext
     * @throws javax.naming.NamingException
     */
	public static void bindJTAUserTransactionImplementation(InitialContext initialContext) throws javax.naming.NamingException
	{
        /** Look up and instantiate an instance of the configured user transaction implementation **/
        String utImplementation = jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionClassName();

        /** Bind the user transaction to the appropriate JNDI context **/
        Reference ref = new Reference(utImplementation, utImplementation, null);
        initialContext.rebind(getUserTransactionJNDIName(), ref);
	}

	/**
	 * Bind the currently configured TransactionSynchronizationRegistry implementation to the default JNDI
	 * context.
	 * @throws javax.naming.NamingException
	 */
	public static void bindJTATransactionSynchronizationRegistryImplementation() throws javax.naming.NamingException
	{
		bindJTATransactionSynchronizationRegistryImplementation(new InitialContext());
	}

    /**
     * Unbind the TSR from the default JNDI context.
     * @throws javax.naming.NamingException
     */
    public static void unbindJTATransactionSynchronizationRegistryImplementation() throws javax.naming.NamingException
	{
		unbindJTATransactionSynchronizationRegistryImplementation(new InitialContext());
	}

	/**
     * Bind the currently configured TransactionSynchronizationRegistry implementation to the passed in
     * JNDI context.
     * @param initialContext
     * @throws javax.naming.NamingException
     */
	public static void bindJTATransactionSynchronizationRegistryImplementation(InitialContext initialContext) throws javax.naming.NamingException
	{
        /** Look up and instantiate an instance of the configured TransactionSynchronizationRegistry implementation **/
        String tsrImplementation = jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistryClassName();
        Object tsr = null;
        try {
            tsr = Class.forName(tsrImplementation).newInstance();
        } catch(Exception e) {
            NamingException namingException = new ConfigurationException(jtaLogger.i18NLogger.get_utils_JNDIManager_tsr1());
            namingException.setRootCause(e);
            throw namingException;
        }

        /** Bind the TransactionSynchronizationRegistry to the appropriate JNDI context **/
        initialContext.rebind(getTransactionSynchronizationRegistryJNDIName(), tsr);
    }

    /**
     * Unbind the TSR from the provided JNDI context.
     * @param initialContext
     * @throws javax.naming.NamingException
     */
    public static void unbindJTATransactionSynchronizationRegistryImplementation(InitialContext initialContext) throws javax.naming.NamingException
	{
        initialContext.unbind(getTransactionSynchronizationRegistryJNDIName());
    }

	private final static String getTransactionManagerJNDIName()
	{
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext();
	}

	private final static String getUserTransactionJNDIName()
	{
        return jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionJNDIContext();
	}

	private final static String getTransactionSynchronizationRegistryJNDIName()
	{
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistryJNDIContext();
	}
}
