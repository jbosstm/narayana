/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.jta.common;

import java.util.List;

/**
 * A JMX MBean interface containing configuration for the JTA system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface JTAEnvironmentBeanMBean
{
    boolean isSupportSubtransactions();

    void setSupportSubtransactions(boolean supportSubtransactions);

    String getTransactionManagerClassName();

    void setTransactionManagerClassName(String jtaTMImplementation);

    String getUserTransactionClassName();

    void setUserTransactionClassName(String jtaUTImplementation);

    String getTransactionSynchronizationRegistryClassName();

    void setTransactionSynchronizationRegistryClassName(String jtaTSRImplementation);

    List<String> getXaRecoveryNodes();

    void setXaRecoveryNodes(List<String> xaRecoveryNodes);

    List<String> getXaResourceRecoveryClassNames();

    void setXaResourceRecoveryClassNames(List<String> xaResourceRecoveryInstances);

    List<String> getXaResourceOrphanFilters();

    void setXaResourceOrphanFilters(List<String> xaResourceOrphanFilters);

    boolean isXaRollbackOptimization();

    void setXaRollbackOptimization(boolean xaRollbackOptimization);

    boolean isXaAssumeRecoveryComplete();

    void setXaAssumeRecoveryComplete(boolean xaAssumeRecoveryComplete);

    String getUserTransactionJNDIContext();

    void setUserTransactionJNDIContext(String jtaUTJNDIContext);

    String getTransactionManagerJNDIContext();

    void setTransactionManagerJNDIContext(String jtaTMJNDIContext);

    String getTransactionSynchronizationRegistryJNDIContext();

    void setTransactionSynchronizationRegistryJNDIContext(String jtaTSRJNDIContext);

    List<String> getXaErrorHandlers();

    void setXaErrorHandlers(List<String> xaErrorHandlers);
    
    boolean isXaTransactionTimeoutEnabled();

    void setXaTransactionTimeoutEnabled(boolean xaTransactionTimeoutEnabled);

    String getLastResourceOptimisationInterface();

    void setLastResourceOptimisationInterface(String lastResourceOptimisationInterface);
}
