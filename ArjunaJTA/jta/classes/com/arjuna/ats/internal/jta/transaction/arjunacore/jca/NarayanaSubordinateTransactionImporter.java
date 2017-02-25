/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import org.jboss.tm.SubordinateTransactionImporter;

/**
 * <p>
 * Subordinate transaction importer which uses {@link SubordinationManager}
 * to get the transaction being imported.
 * <p>
 * This is implementation of generic spi interface which is used when we import subordinate transaction
 * to transaction manager implementation different to Narayana implementation.
 * <p>
 * This happens e.g. in WFLY where wildfly-transaction-client defines it's own transaction
 * manager and we need to import transaction there.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class NarayanaSubordinateTransactionImporter implements SubordinateTransactionImporter {

    /**
     * {@inheritDoc}
     */
    public Transaction getTransaction(Xid xid) throws XAException {
        return SubordinationManager.getTransactionImporter().importTransaction(xid);
    }

}
