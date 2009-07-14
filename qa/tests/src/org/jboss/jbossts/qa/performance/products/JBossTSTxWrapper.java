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

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

import javax.transaction.Transaction;
import javax.transaction.SystemException;

/**
 * TODO
 */
public class JBossTSTxWrapper implements TxWrapper
{
    AtomicAction tx;

    public JBossTSTxWrapper()
    {
    }

    public TxWrapper createWrapper()
    {
        return new JBossTSTxWrapper();
    }

    public Transaction getTransaction() throws SystemException
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
    }

    public int begin()
    {
        this.tx = new AtomicAction();
        
        return tx.begin();
    }

    public int commit()
    {
        return tx.commit();
    }

    public int abort()
    {
        return tx.abort();
    }

    public int add(AbstractRecord record)
    {
        return tx.add(record);
    }

    public boolean supportsNestedTx()
    {
        return true;
    }

    public String getName()
    {
        return "JBossTS";
    }
}
