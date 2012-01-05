/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.performance.products;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

import javax.transaction.Transaction;
import javax.transaction.SystemException;

/**
 * Minimal interface that TM products should implement for controlling
 * transactions.
 * @see BaseWrapper for the default implementation of this interface from which
 * other implementations may subclass
 */
public interface TxWrapper
{
    /**
     * Objtain a new wrapper
     * @return a wrapper arround a real transaction
     */
    TxWrapper createWrapper();

    /**
     * Start a new transaction
     * @return one of the com.arjuna.ats.arjuna.coordinator.ActionStatus constants
     */
    int begin();

    /**
     * Commit the current transaction
     * @return one of the com.arjuna.ats.arjuna.coordinator.ActionStatus constants
     */
    int commit();
    int abort();

    /**
     * Enlist a resource within the current transaction. For JTA products other than JBossTS
     * this record should be converted to an XAResource and enlisted with the transaction
     * @param record
     * @return one of the com.arjuna.ats.arjuna.coordinator.AddOutcome constants
     */
    int add(AbstractRecord record);
    Transaction getTransaction() throws SystemException;
    boolean supportsNestedTx();
    String getName();
}
