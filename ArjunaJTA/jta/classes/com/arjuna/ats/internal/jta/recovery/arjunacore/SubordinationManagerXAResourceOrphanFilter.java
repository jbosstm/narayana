/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2016,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.Vector;

/**
 * An XAResourceOrphanFilter which vetos rollback for xids which have an in-flight subordinate transaction.
 * <p>
 * The SubordinateAtomicActionRecoveryModule must be loaded and in a position prior to the XARecoveryModule within the list
 * of recovery modules for this to work so we verify that during orphan detection.
 */
public class SubordinationManagerXAResourceOrphanFilter implements XAResourceOrphanFilter {
    private SubordinateAtomicActionRecoveryModule subordinateAtomicActionRecoveryModule;

    @Override
    public Vote checkXid(Xid xid) {
        if (xid.getFormatId() != XATxConverter.FORMAT_ID) {
            // we only care about Xids created by the JTA
            return Vote.ABSTAIN;
        }

        List<String> _xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();

        if(_xaRecoveryNodes == null || _xaRecoveryNodes.size() == 0) {
            jtaLogger.i18NLogger.info_recovery_noxanodes();
            return Vote.ABSTAIN;
        }

        String nodeName = XATxConverter.getSubordinateNodeName(new XidImple(xid).getXID());

        if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("subordinate node name of " + xid + " is " + nodeName);
        }

        if (!_xaRecoveryNodes.contains(nodeName)) {
            // It either doesn't have a subordinate node name or isn't for this server
            return Vote.ABSTAIN;
        }

        if (!getSubordinateAtomicActionRecoveryModule().isRecoveryScanCompletedWithoutError()) {
            // If any errors were encountered by the SubordinateAtomicActionRecoveryModule then it is possible
            // the TransactionImple has not been loaded yet so we have to tell the XARecoveryModule to leave this
            // Xid alone
            return Vote.LEAVE_ALONE;
        }

        XidImple theXid = new XidImple(xid);
        SubordinateTransaction importedTransaction = null;
        try {
            importedTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(theXid);
        } catch (XAException e) {
            return Vote.LEAVE_ALONE;
        }
        if (importedTransaction != null) {
            return Vote.LEAVE_ALONE;
        } else {
            return Vote.ROLLBACK;
        }
    }

    /**
     * This method retrieves a reference to the SubordinateAtomicActionRecoveryModule so that we can verify there
     * were no failures in recovery transactions for this pass of the recovery cycle.
     *
     * @return a reference to the SubordinateAtomicActionRecoveryModule that is in use
     */
    private SubordinateAtomicActionRecoveryModule getSubordinateAtomicActionRecoveryModule() {
        if (this.subordinateAtomicActionRecoveryModule == null) {
            Vector<RecoveryModule> modules = RecoveryManager.manager().getModules();
            for (RecoveryModule module : modules) {
                if (module.getClass().equals(SubordinateAtomicActionRecoveryModule.class)) {
                    this.subordinateAtomicActionRecoveryModule = (SubordinateAtomicActionRecoveryModule) module;
                }
            }
        }
        return this.subordinateAtomicActionRecoveryModule;
    }
}
