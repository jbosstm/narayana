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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jta;

import org.jboss.tm.*;
import org.jboss.logging.Logger;

import com.arjuna.ats.internal.jbossatx.jta.jca.XATerminator;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.common.Configuration;

import com.arjuna.common.util.logging.LogFactory;

import javax.naming.Reference;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * JBoss Transaction Manager Service.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
public class TransactionManagerService implements TransactionManagerServiceMBean
{
    /*
    deploy/transaction-jboss-beans.xml:

    <?xml version="1.0" encoding="UTF-8"?>
    <deployment xmlns="urn:jboss:bean-deployer:2.0">

    <bean name="TransactionManager" class="com.arjuna.ats.jbossatx.jta.TransactionManagerService">
        <annotation>@org.jboss.aop.microcontainer.aspects.jmx.JMX(name="jboss:service=TransactionManager", exposedInterface=com.arjuna.ats.jbossatx.jta.TransactionManagerServiceMBean.class, registerDirectly=true)</annotation>

        <property name="transactionTimeout">300</property>
        <property name="objectStoreDir">${jboss.server.data.dir}/tx-object-store</property>
        <property name="mbeanServer"><inject bean="JMXKernel" property="mbeanServer"/></property>
    </bean>

    </deployment>
     */

    static {
		/*
		 * Override the default logging config, force use of the plugin that rewrites log levels to reflect app server level semantics.
		 * This must be done before the loading of anything that uses the logging, otherwise it's too late to take effect.
		 * Hence the static initializer block.
		 * see also http://jira.jboss.com/jira/browse/JBTM-20
		 */
		com.arjuna.ats.arjuna.common.arjPropertyManager.getPropertyManager().setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler");
		//System.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler") ;
	}

    private final Logger log = org.jboss.logging.Logger.getLogger(TransactionManagerService.class);

    private final static String PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextImporter";
    private final static String PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextExporter";
    private static final JBossXATerminator TERMINATOR = new XATerminator() ;

    public TransactionManagerService() {}


    public void create() throws Exception
    {
        // Note that we use the arjunacore version of Configuration, as the jta one does not have
        // build properties set when we are running from the jts version of the build.
        String tag = Configuration.getBuildTimeProperty("SOURCEID");

        log.info("JBossTS Transaction Service (JTA version - tag:"+tag+") - JBoss Inc.");

        // Associate transaction reaper with our context classloader.
        TransactionReaper.create() ;

        /** Register propagation context manager **/
        try
        {
            /** Bind the propagation context manager **/
            bindRef(PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jta.PropagationContextManager.class.getName());
            bindRef(PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jta.PropagationContextManager.class.getName());
        }
        catch (Exception e)
        {
            log.fatal("Failed to create and register Propagation Context Manager", e);
        }

        /** Bind the transaction manager and tsr JNDI references **/
        log.info("Binding TransactionManager JNDI Reference");

        jtaPropertyManager.getJTAEnvironmentBean().setJtaTMImplementation(TransactionManagerDelegate.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setJtaUTImplementation(UserTransactionImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setJtaTSRImplementation(TransactionSynchronizationRegistryImple.class.getName());

		// When running inside the app server, we bind TSR in the JNDI java:/ space, not its required location.
		// It's the job of individual components (EJB3, web, etc) to copy the ref to the java:/comp space)
        jtaPropertyManager.getJTAEnvironmentBean().setJtaTSRJNDIContext("java:/TransactionSynchronizationRegistry");

		JNDIManager.bindJTATransactionManagerImplementation();
		JNDIManager.bindJTATransactionSynchronizationRegistryImplementation();
	}

    public void destroy()
    {
        log.info("Destroying TransactionManagerService");

        // unregister the JNDI entries that were registered by create()
        try
        {
            unbind(PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE);
            unbind(PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE);

            JNDIManager.unbindJTATransactionManagerImplementation();
            JNDIManager.unbindJTATransactionSynchronizationRegistryImplementation();
        }
        catch(NamingException e)
        {
            log.warn("Unable to unbind TransactionManagerService JNDI entries ", e);
        }
    }

    public void start()
    {
    }

    public void stop()
    {
    }


    /**
     * Retrieve a reference to the JTA transaction manager.
     *
     * @return A reference to the JTA transaction manager.
     */
    public TransactionManager getTransactionManager()
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    /**
     * Retrieve a reference ot the JTA TransactionSynchronizationRegistry.
     *
     * @return a reference to the JTA TransactionSynchronizationRegistry.
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
    {
        // rely on the imple being stateless:
        return new TransactionSynchronizationRegistryImple();
    }

    /**
     * Get the XA Terminator
     *
     * @return the XA Terminator
     */
    public JBossXATerminator getXATerminator()
    {
       return TERMINATOR ;
    }

    /**
     * Retrieve a reference to the JTA user transaction manager.
     *
     * @return A reference to the JTA user transaction manager.
     */
    public UserTransaction getUserTransaction()
    {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void registerXAExceptionFormatter(Class c, XAExceptionFormatter f)
    {
        log.warn("XAExceptionFormatters are not supported by the JBossTS Transaction Service - this warning can safely be ignored");
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void unregisterXAExceptionFormatter(Class c)
    {
        // Ignore
    }

    private void bindRef(String jndiName, String className)
            throws Exception
    {
        Reference ref = new Reference(className, className, null);
        new InitialContext().bind(jndiName, ref);
    }

    private void unbind(String jndiName) throws NamingException
    {
        new InitialContext().unbind(jndiName);
    }
}
