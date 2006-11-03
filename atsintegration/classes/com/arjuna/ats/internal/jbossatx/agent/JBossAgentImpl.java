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
 * $Id: JBossAgentImpl.java,v 1.4 2005/06/15 14:04:02 kconner Exp $
 */
package com.arjuna.ats.internal.jbossatx.agent;

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import com.arjuna.ats.tsmx.agent.AgentInterface;
import com.arjuna.ats.tsmx.agent.exceptions.AgentNotFoundException;

public class JBossAgentImpl implements AgentInterface
{
	private MBeanServer _server = null;

	public JBossAgentImpl()
	{
		final String providerUrl = System.getProperty(InitialContext.PROVIDER_URL, "jnp://localhost:1099");
		final Hashtable env = new Hashtable();
		env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		env.put(InitialContext.PROVIDER_URL, providerUrl);
		env.put(InitialContext.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		final InitialContext ctx ;
		try
		{
			ctx = new InitialContext(env);
		}
		catch (final NamingException ne)
		{
			throw new ExceptionInInitializerError("Failed to initialize naming context: " + ne);
		}
		
		RMIAdaptor adaptor = null ;
		try
		{
			adaptor = (RMIAdaptor)ctx.lookup("jmx/rmi/RMIAdaptor") ;
		}
		catch (final NamingException ne)
		{
			try
			{
				adaptor = (RMIAdaptor)ctx.lookup("jmx/invoker/RMIAdaptor") ;
			}
			catch (final NamingException ne2)
			{
				throw new ExceptionInInitializerError("Failed to retrieve reference to remote MBean server: " + ne2) ;
			}
		}
		
		final Class serverClass = MBeanServer.class ;
		_server = (MBeanServer)Proxy.newProxyInstance(serverClass.getClassLoader(),
				new Class[] {serverClass},new RemoteInvocationHandler(adaptor)) ;
	}

	public MBeanServer getAgent() throws AgentNotFoundException
	{
		if (_server == null)
			throw new AgentNotFoundException("Failed to lookup JBOSS agent");

		return _server;
	}
	
	/**
     * Remote Invocation Handler using RMIAdaptor.
     * 
     * @author kevin
     */
    public static class RemoteInvocationHandler implements InvocationHandler
    {
        /**
         * The adaptor class.
         */
        private static final Class ADAPTOR_CLASS = RMIAdaptor.class ;

        /**
         * The RMI adaptor.
         */
        private final RMIAdaptor adaptor ;

        /**
         * Construct the Remote Invocation Handler.
         * 
         * @param adaptor
         */
        public RemoteInvocationHandler(final RMIAdaptor adaptor)
        {
            this.adaptor = adaptor ;
        }

        /**
         * Invoke the method.
         * 
         * @param proxy The current proxy.
         * @param method The method to invoke.
         * @param args The arguments for the invocation.
         * @return The result.
         * @throws Throwable For any exceptions.
         */
        public Object invoke(final Object proxy, final Method method,
                final Object[] args) throws Throwable
        {
            final String methodName = method.getName() ;
            final Method adaptorMethod ;
            try
            {
                adaptorMethod = ADAPTOR_CLASS.getMethod(methodName, method
                        .getParameterTypes()) ;
            }
            catch (final NoSuchMethodException nsme)
            {
                throw new UnsupportedOperationException("Operation "
                        + methodName + " not supported with remote proxy") ;
            }

            try
            {
                return adaptorMethod.invoke(adaptor, args) ;
            }
            catch (final InvocationTargetException ite)
            {
                final Throwable target = ite.getTargetException() ;
                if (target instanceof IOException)
                {
                    throw new RuntimeIOException(target) ;
                }
                throw target ;
            }
        }
    }

    /**
     * Runtime checked Exception
     * @author kevin
     */
    public static class RuntimeIOException extends RuntimeException
    {
        /**
         * The serial version uid.
         */
        private static final long serialVersionUID = 3976733670713669941L ;
        /**
         * The nested throwable.
         */
        private final Throwable nestedThrowable ;

        /**
         * Construct the runtime exception with the associated throwable.
         * @param nestedThrowable The wrapped throwable.
         */
        public RuntimeIOException(final Throwable nestedThrowable)
        {
            this.nestedThrowable = nestedThrowable ;
        }

        /**
         * Get the localized message.
         * @return the localized message.
         */
        public String getLocalizedMessage()
        {
            return nestedThrowable.getLocalizedMessage() ;
        }

        /**
         * Get the message.
         * @return the message.
         */
        public String getMessage()
        {
            return nestedThrowable.getMessage() ;
        }

        /**
         * Get the stack trace elements.
         * @return the stack trace elements.
         */
        public StackTraceElement[] getStackTrace()
        {
            return nestedThrowable.getStackTrace() ;
        }

        /**
         * Print the stack trace.
         */
        public void printStackTrace()
        {
            nestedThrowable.printStackTrace() ;
        }

        /**
         * Print the stack trace to the specified print stream.
         * @param ps The print stream.
         */
        public void printStackTrace(final PrintStream ps)
        {
            nestedThrowable.printStackTrace(ps) ;
        }

        /**
         * Print the stack trace to the specified print writer.
         * @param pw The print writer.
         */
        public void printStackTrace(final PrintWriter pw)
        {
            nestedThrowable.printStackTrace(pw) ;
        }
    }
}
