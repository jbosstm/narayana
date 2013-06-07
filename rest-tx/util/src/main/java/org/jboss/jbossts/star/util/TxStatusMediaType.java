/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
 */
package org.jboss.jbossts.star.util;

public class TxStatusMediaType extends TxMediaType{
    public static final String STATUS_PROPERTY = "txstatus";

    public static final String TX_ACTIVE = toMediaType(TxStatus.TransactionActive);
    public static final String TX_PREPARED = toMediaType(TxStatus.TransactionPrepared);
    public static final String TX_COMMITTED = toMediaType(TxStatus.TransactionCommitted);
    public static final String TX_ROLLEDBACK = toMediaType(TxStatus.TransactionRolledBack);
    public static final String TX_ROLLBACK_ONLY = toMediaType(TxStatus.TransactionRollbackOnly);
    public static final String TX_COMMITTED_ONE_PHASE = toMediaType(TxStatus.TransactionCommittedOnePhase);
    public static final String TX_H_MIXED = toMediaType(TxStatus.TransactionHeuristicMixed);
    public static final String TX_H_ROLLBACK = toMediaType(TxStatus.TransactionHeuristicRollback);

    public static String toMediaType(TxStatus txStatus) {
        return new StringBuilder(TxStatusMediaType.STATUS_PROPERTY).append('=').append(txStatus.name()).toString();
    }

}
