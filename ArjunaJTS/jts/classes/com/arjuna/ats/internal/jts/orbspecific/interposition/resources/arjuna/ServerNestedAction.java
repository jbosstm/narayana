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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Hewlett Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerNestedAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * This looks like an atomic action, but is not actually derived from
 * BasicAction or OTS_Transaction. This is because of the way in which the OTS
 * creates and manipulates transactions. This is a nested action proxy.
 */

public class ServerNestedAction extends ServerResource implements
        org.omg.CosTransactions.SubtransactionAwareResourceOperations
{

    /**
     * Create local transactions with same ids as remote.
     */

    public ServerNestedAction(ServerControl myControl)
    {
        super(myControl);

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ServerNestedAction::ServerNestedAction ( " + _theUid
                    + " )");
        }

        _theResource = null;
        _resourceRef = null;

        if (_theControl != null)
        {
            _theResource = new org.omg.CosTransactions.SubtransactionAwareResourcePOATie(
                    this);

            ORBManager.getPOA().objectIsReady(_theResource);

            _resourceRef = org.omg.CosTransactions.SubtransactionAwareResourceHelper
                    .narrow(ORBManager.getPOA().corbaReference(_theResource));

            /*
             * Would like to be able to attach a thread filter to this object if
             * process-filters aren't supported. However, currently this won't
             * work as we can't have two different filter types working at the
             * same time. ATTACH_THREAD_FILTER_(_theResource);
             */

            Coordinator realCoordinator = _theControl.originalCoordinator();

            if (!(_valid = registerSubTran(realCoordinator))) {
                jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_ipfailed_2("ServerNestedAction");

                /*
                 * Failed to register. Valid is set, and the interposition
                 * controller will now deal with this.
                 */

                realCoordinator = null;
            }
        }
    }

    public void commit_subtransaction (Coordinator parent)
            throws SystemException
    {
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ServerNestedAction::commit_subtransaction : " + _theUid);
        }

        if (_theControl == null) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_nullcontrol_1(
                    "ServerNestedAction.commit_subtransaction");

            throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL,
                    CompletionStatus.COMPLETED_NO);
        }

        if (_theControl.isWrapper())
        {
            destroyResource();
            return;
        }

        ServerTransaction theTransaction = (ServerTransaction) _theControl
                .getImplHandle();

        // ThreadActionData.pushAction(theTransaction);

        /*
         * Do nothing about propagation since we should already be registered
         * with the parent, i.e., ignore the parent parameter.
         */

        /*
         * We should not get exceptions here.
         */

        try
        {
            theTransaction.commit(false);
        }
        catch (TRANSACTION_ROLLEDBACK e1) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e1);

            throw e1;
        }
        catch (INVALID_TRANSACTION e5) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e5);

            throw e5;
        }
        catch (HeuristicMixed e2) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e2);

            /*
             * Can't rethrow heuristic exceptions for subtransactions!
             */

            throw new BAD_OPERATION(ExceptionCodes.HEURISTIC_COMMIT,
                    CompletionStatus.COMPLETED_MAYBE);
        }
        catch (HeuristicHazard e3) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e3);

            throw new BAD_OPERATION(ExceptionCodes.HEURISTIC_COMMIT,
                    CompletionStatus.COMPLETED_MAYBE);
        }
        catch (SystemException e4) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e4);

            throw e4;
        }
        catch (Exception e5) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                    "ServerNestedAction.commit_subtransaction", e5);

            throw new UNKNOWN(e5.toString());
        }
        finally
        {
            ThreadActionData.popAction();
            destroyResource();
        }
    }

    public void rollback_subtransaction () throws SystemException
    {
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ServerNestedAction::rollback_subtransaction : " + _theUid);
        }

        if (_theControl == null) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_nullcontrol_2(
                    "ServerNestedAction.rollback_subtransaction");

            throw new INVALID_TRANSACTION(ExceptionCodes.SERVERAA_NO_CONTROL,
                    CompletionStatus.COMPLETED_NO);
        }

        if (_theControl.isWrapper())
        {
            destroyResource();
            return;
        }

        ServerTransaction theTransaction = (ServerTransaction) _theControl
                .getImplHandle();

        // ThreadActionData.pushAction(theTransaction);

        try
        {
            if (!valid())
                theTransaction.doPhase2Abort();
            else
                theTransaction.rollback();
        }
        catch (SystemException e)
        {
            throw e;
        }
        catch (Exception ex)
        {
            throw new UNKNOWN(ex.toString());
        }
        finally
        {
            ThreadActionData.popAction();
            destroyResource();
        }
    }

    /*
     * These methods should never be called.
     */

    public org.omg.CosTransactions.Vote prepare () throws SystemException,
            HeuristicMixed, HeuristicHazard
    {
        throw new BAD_OPERATION(ExceptionCodes.SERVERAA_PREPARE,
                CompletionStatus.COMPLETED_NO);
    }

    public void rollback () throws SystemException, HeuristicCommit,
            HeuristicMixed, HeuristicHazard
    {
    }

    public void commit () throws SystemException, NotPrepared,
            HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
    }

    public void forget () throws SystemException
    {
    }

    public void commit_one_phase () throws HeuristicHazard, SystemException
    {
    }

    public SubtransactionAwareResource theResource ()
    {
        return _resourceRef;
    }

    protected ServerNestedAction()
    {
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ServerNestedAction::ServerNestedAction ()");
        }

        _theResource = null;
        _resourceRef = null;
    }

    protected final synchronized void destroyResource ()
    {
        if (!_destroyed)
        {
            _destroyed = true;

            if (_parent != null)
            {
                /*
                 * Now try to garbage collect this resource. Since it was
                 * registered as a subtranaware resource it won't get called
                 * again.
                 */

                if (!_parent.removeChild(this)) {
                    jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_childerror(
                            get_uid(), _parent.get_uid());
                }
            }

            if (_theResource != null)
            {
                ORBManager.getPOA().shutdownObject(_theResource);
                _theResource = null;
            }
        }

        tidyup();
    }

    protected boolean registerSubTran (Coordinator theCoordinator)
    {
        boolean result = false;

        if (theCoordinator != null)
        {
            try
            {
                theCoordinator.register_subtran_aware(_resourceRef);
                result = true;
            }
            catch (Inactive e) {
                jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                        "ServerNestedAction.registerSubTran", e);
            }
            catch (NotSubtransaction e) {
                jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                        "ServerNestedAction.registerSubTran", e);
            }
            catch (SystemException e) {
                jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_generror_2(
                        "ServerNestedAction.registerSubTran", e);
            }
        }
        else {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_arjuna_nullcoord(
                    "ServerNestedAction.registerSubTran");
        }

        return result;
    }

    protected org.omg.CosTransactions.SubtransactionAwareResourcePOATie _theResource;

    protected SubtransactionAwareResource _resourceRef;

}
