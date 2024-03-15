/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.resources.arjunacore;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ExceptionDeferrer;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.XAResourceErrorHandler;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.common.internal.util.ClassloadingUtility;
import org.jboss.tm.FirstResource;
import org.jboss.tm.LastResource;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: XAResourceRecord.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.2.4.
 */

public class XAResourceRecord extends AbstractRecord implements ExceptionDeferrer {

    public static final int XACONNECTION = 0;

    private static final Uid START_XARESOURCE = Uid.minUid();

    private static final Uid END_XARESOURCE = Uid.maxUid();

    /**
     * Any XAException that occurs.
     */
    List<Throwable> deferredExceptions;

    /**
     * The params represent specific parameters we need to recreate the
     * connection to the database in the event of a failure. If they're not set
     * then recovery is out of our control.
     * <p>
     * Could also use it to pass other information, such as the readonly flag.
     */

    public XAResourceRecord(TransactionImple tx, XAResource res, Xid xid,
                            Object[] params) {
        super(new Uid(), null, ObjectType.ANDPERSISTENT);

        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.XAResourceRecord ( " + xid + ", " + res + " ), record id=" + order());
        }

        _theXAResource = res;
        if (_xaResourceRecordWrappingPlugin != null) {
            _xaResourceRecordWrappingPlugin.transcribeWrapperData(this);
        }

        _recoveryObject = null;
        _tranID = xid;

        _valid = true;

        if (params != null) {
            if (params.length > XACONNECTION) {
                if (params[XACONNECTION] instanceof RecoverableXAConnection)
                    _recoveryObject = (RecoverableXAConnection) params[XACONNECTION];
            }
        }

        _prepared = false;
        _heuristic = TwoPhaseOutcome.FINISH_OK;

        _theTransaction = tx;
    }

    public final Xid getXid() {
        return _tranID;
    }

    public Uid order() {
        if (_theXAResource instanceof FirstResource)
            return START_XARESOURCE;
        else if (_theXAResource instanceof LastResource)
            return END_XARESOURCE;

        return super.order();
    }

    public boolean propagateOnCommit() {
        // cannot ever be nested!
        return false;
    }

    public int typeIs() {
        return RecordType.JTA_RECORD;
    }

    public Object value() {
        return _theXAResource;
    }

    public void setValue(Object o) {
        jtaLogger.i18NLogger.warn_resources_arjunacore_setvalue("XAResourceRecord::set_value()");
    }

    public int nestedAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /*
     * XA is not subtransaction aware.
     */

    public int nestedPrepare() {
        // do nothing
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public int topLevelPrepare() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.topLevelPrepare for " + this + ", record id=" + order());
        }

        if (!_valid || (_theXAResource == null) || (_tranID == null)) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_preparenulltx("XAResourceRecord.prepare");

            removeConnection();

            return TwoPhaseOutcome.PREPARE_NOTOK;
        }

        try {
            endAssociation(XAResource.TMSUCCESS, TxInfo.NOT_ASSOCIATED);

            _prepared = true;

            if (_theXAResource.prepare(_tranID) == XAResource.XA_RDONLY) {
                if (TxControl.isReadonlyOptimisation()) {
                    // we won't be called again, so we need to tidy up now
                    removeConnection();
                }

                return TwoPhaseOutcome.PREPARE_READONLY;
            } else
                return TwoPhaseOutcome.PREPARE_OK;
        } catch (XAException e1) {
            addDeferredThrowable(e1);

            jtaLogger.i18NLogger.warn_resources_arjunacore_preparefailed(XAHelper.xidToString(_tranID),
                    _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

            /*
             * XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
             * XAER_PROTO.
             */

            switch (e1.errorCode) {
                // No txn -> assume rollback
                case XAException.XAER_NOTA:
                    // The XA resource already rolled back the transaction branch
                case XAException.XA_RBROLLBACK:
                case XAException.XA_RBEND:
                case XAException.XA_RBCOMMFAIL:
                case XAException.XA_RBDEADLOCK:
                case XAException.XA_RBINTEGRITY:
                case XAException.XA_RBOTHER:
                case XAException.XA_RBPROTO:
                case XAException.XA_RBTIMEOUT:
                    // This will avoid calling rollback when aborting
                    _rolledBack = true;
                    return TwoPhaseOutcome.PREPARE_NOTOK;
                case XAException.XAER_INVAL:
                case XAException.XAER_PROTO:
                case XAException.XAER_RMERR:
                case XAException.XAER_RMFAIL:
                    // We might need to roll back the XA resource for these error codes
                    return TwoPhaseOutcome.PREPARE_NOTOK;
                default:
                    // we're not really sure (shouldn't get here though).
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }
        } catch (Exception e2) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_preparefailed(XAHelper.xidToString(_tranID),
                    _theXAResource.toString(), "-", e2);

            return TwoPhaseOutcome.PREPARE_NOTOK;
        }
    }

    public int topLevelAbort() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.topLevelAbort for " + this + ", record id=" + order());
        }

        if (!_valid)
            return TwoPhaseOutcome.FINISH_ERROR;

        if (_theTransaction != null
                && _theTransaction.getXAResourceState(_theXAResource) == TxInfo.OPTIMIZED_ROLLBACK) {

            /*
             * Already rolledback during delist.
             */

            return TwoPhaseOutcome.FINISH_OK;
        }

        if (_tranID == null) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_rollbacknulltx("XAResourceRecord.rollback");

            return TwoPhaseOutcome.FINISH_ERROR;
        } else {
            if (_theXAResource == null)
                _theXAResource = getNewXAResource();

            if (_theXAResource != null) {
                if (_heuristic != TwoPhaseOutcome.FINISH_OK)
                    return _heuristic;

                try {
                    if (!_prepared) {
                        endAssociation(XAResource.TMFAIL, TxInfo.FAILED);
                    }
                } catch (XAException e1) {
                    addDeferredThrowable(e1);

                    if ((e1.errorCode >= XAException.XA_RBBASE)
                            && (e1.errorCode < XAException.XA_RBEND)) {

                        /*
                         * Has been marked as rollback-only. If the branch has not been
                         * already rolled back (i.e. _rolledBack == true ), we still need
                         * to call rollback.
                         */

                    } else if ((e1.errorCode == XAException.XAER_RMERR) || (e1.errorCode == XAException.XAER_RMFAIL)) {
                        try {
                            if (!this._rolledBack) {
                                _theXAResource.rollback(_tranID);
                                this._rolledBack = true;
                            }
                        } catch (XAException e2) {
                            addDeferredThrowable(e2);

                            jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(_tranID),
                                    _theXAResource.toString(), XAHelper.printXAErrorCode(e2), e2);

                            removeConnection();

                            return TwoPhaseOutcome.FINISH_ERROR;
                        }
                    } else {
                        jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(_tranID),
                                _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

                        removeConnection();

                        return TwoPhaseOutcome.FINISH_ERROR;
                    }
                } catch (RuntimeException e) {
                    jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(_tranID), _theXAResource.toString(), "-", e);

                    throw e;
                }

                try {
                    if (!this._rolledBack) {
                        _theXAResource.rollback(_tranID);
                        this._rolledBack = true;
                    }
                } catch (XAException e1) {
                    if (notAProblem(e1, false)) {
                        // some other thread got there first (probably)
                    } else {
                        addDeferredThrowable(e1);

                        jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(_tranID),
                                _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

                        switch (e1.errorCode) {
                            case XAException.XAER_RMERR:
                                if (!_prepared)
                                    // just do the finally block
                                    break;
                            case XAException.XA_HEURHAZ:
                                _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                                return TwoPhaseOutcome.HEURISTIC_HAZARD;
                            case XAException.XA_HEURCOM:
                                _heuristic = TwoPhaseOutcome.HEURISTIC_COMMIT;
                                return TwoPhaseOutcome.HEURISTIC_COMMIT;
                            case XAException.XA_HEURMIX:
                                _heuristic = TwoPhaseOutcome.HEURISTIC_MIXED;
                                return TwoPhaseOutcome.HEURISTIC_MIXED;
                            case XAException.XAER_NOTA:
                                if (_recovered)
                                    // rolled back previously and recovery completed
                                    break;
                                // forget?
                            case XAException.XA_HEURRB:
                            case XAException.XA_RBROLLBACK:
                            case XAException.XA_RBEND:
                            case XAException.XA_RBCOMMFAIL:
                            case XAException.XA_RBDEADLOCK:
                            case XAException.XA_RBINTEGRITY:
                            case XAException.XA_RBOTHER:
                            case XAException.XA_RBPROTO:
                            case XAException.XA_RBTIMEOUT:

                                /** The XA resource has rolled back the
                                 * transaction branch’s work and has released
                                 * all held resources. Nothing left to do
                                 */

                                this._rolledBack = true;
                                break;
                            default:
                                return TwoPhaseOutcome.FINISH_ERROR;
                        }
                    }
                } catch (Exception e2) {
                    jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(_tranID),
                            _theXAResource.toString(), "-", e2);

                    return TwoPhaseOutcome.FINISH_ERROR;
                } finally {
                    if (!_prepared)
                        removeConnection();
                }
            } else {
                jtaLogger.i18NLogger.warn_resources_arjunacore_noresource(XAHelper.xidToString(_tranID));

                if (XAResourceRecord._assumedComplete) {
                    jtaLogger.i18NLogger.info_resources_arjunacore_assumecomplete(XAHelper.xidToString(_tranID));


                    return TwoPhaseOutcome.FINISH_OK;
                } else
                    return TwoPhaseOutcome.FINISH_ERROR;
            }
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.topLevelCommit for " + this + ", record id=" + order());
        }

        if (!_prepared)
            return TwoPhaseOutcome.NOT_PREPARED;

        if (_tranID == null) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_commitnulltx("XAResourceRecord.commit");

            return TwoPhaseOutcome.FINISH_ERROR;
        } else {
            if (_theXAResource == null)
                _theXAResource = getNewXAResource();

            if (_theXAResource != null) {
                if (_heuristic != TwoPhaseOutcome.FINISH_OK)
                    return _heuristic;

                /*
                 * No need for end call here since we can only get to this
                 * point by going through prepare.
                 */

                try {
                    _theXAResource.commit(_tranID, false);
                } catch (XAException e1) {
                    if (notAProblem(e1, true)) {
                        // some other thread got there first (probably)
                    } else {
                        addDeferredThrowable(e1);

                        jtaLogger.i18NLogger.warn_resources_arjunacore_commitxaerror(XAHelper.xidToString(_tranID),
                                _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

                        /*
                         * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
                         * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
                         * XAER_PROTO.
                         */

                        switch (e1.errorCode) {
                            case XAException.XA_HEURHAZ:
                                _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                                return TwoPhaseOutcome.HEURISTIC_HAZARD;
                            // what about forget?
                            case XAException.XA_HEURCOM:
                                // OTS doesn't support this code here.
                                break;
                            case XAException.XA_HEURRB:
                                // could really do with an ABORTED status in TwoPhaseOutcome to differentiate
                            case XAException.XA_RBROLLBACK:
                            case XAException.XA_RBCOMMFAIL:
                            case XAException.XA_RBDEADLOCK:
                            case XAException.XA_RBINTEGRITY:
                            case XAException.XA_RBOTHER:
                            case XAException.XA_RBPROTO:
                            case XAException.XA_RBTIMEOUT:
                            case XAException.XA_RBTRANSIENT:
                            case XAException.XAER_RMERR:
                            case XAException.XAER_PROTO:
                                // XA spec implies rollback
                                _rolledBack = true;
                                _heuristic = TwoPhaseOutcome.HEURISTIC_ROLLBACK;
                                return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
                            case XAException.XA_HEURMIX:
                                return TwoPhaseOutcome.HEURISTIC_MIXED;
                            case XAException.XAER_NOTA:
                                if (_recovered)
                                    // committed previously and recovery completed
                                    break;
                                else {
                                    _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                                    // something terminated the transaction!
                                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                                }
                            case XAException.XA_RETRY:
                            case XAException.XAER_RMFAIL:
                                // will cause log to be rewritten
                                _committed = true;

                                /*
                                 * Could do timeout retry here, but that could cause other resources in the list to go down the
                                 * heuristic path (some are far too keen to do this). Fail and let recovery retry. Meanwhile
                                 * the coordinator will continue to commit the other resources immediately.
                                 */

                                return TwoPhaseOutcome.FINISH_ERROR;
                            // resource manager failed, did it rollback?
                            case XAException.XAER_INVAL:
                            default:
                                _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                                return TwoPhaseOutcome.HEURISTIC_HAZARD;
                        }
                    }
                } catch (Exception e2) {
                    jtaLogger.i18NLogger.warn_resources_arjunacore_commitxaerror(XAHelper.xidToString(_tranID),
                            _theXAResource.toString(), "-", e2);

                    return TwoPhaseOutcome.FINISH_ERROR;
                } finally {
                    removeConnection();
                }
            } else {
                jtaLogger.i18NLogger.warn_resources_arjunacore_noresource(XAHelper.xidToString(_tranID));

                if (XAResourceRecord._assumedComplete) {
                    jtaLogger.i18NLogger.info_resources_arjunacore_assumecomplete(XAHelper.xidToString(_tranID));

                    return TwoPhaseOutcome.FINISH_OK;
                } else if (_jndiName != null && wasResourceContactedByRecoveryModule(_jndiName)) {
                    jtaLogger.i18NLogger.info_resources_arjunacore_rmcompleted(XAHelper.xidToString(_tranID));
                    return TwoPhaseOutcome.FINISH_OK;
                } else
                    return TwoPhaseOutcome.FINISH_ERROR;
            }
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * Is the XAException a non-error when received in reply to commit or
     * rollback ? It normally is, but may be overridden in recovery.
     */

    protected boolean notAProblem(XAException ex, boolean commit) {
        return XAResourceErrorHandler.notAProblem(_theXAResource, ex, commit);
    }

    public int nestedOnePhaseCommit() {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    /**
     * For commit_one_phase we can do whatever we want since the transaction
     * outcome is whatever we want. Therefore, we do not need to save any
     * additional recoverable state, such as a reference to the transaction
     * coordinator, since it will not have an intentions list anyway.
     */

    public int topLevelOnePhaseCommit() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.topLevelOnePhaseCommit for " + this + ", record id=" + order());
        }

        boolean commit = true;

        if (_tranID == null) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_opcnulltx("XAResourceRecord.1pc");

            // rolled back!!
            _rolledBack = true;
            return TwoPhaseOutcome.ONE_PHASE_ERROR;
        } else {
            if (_theXAResource != null) {
                if (_heuristic != TwoPhaseOutcome.FINISH_OK)
                    return _heuristic;

                XAException endHeuristic = null;
                XAException endRBOnly = null;

                try {

                    /*
                     * TODO in Oracle the end is not needed. Is this common
                     * across other RMs?
                     */

                    endAssociation(XAResource.TMSUCCESS, TxInfo.NOT_ASSOCIATED);
                } catch (XAException e1) {

                    /*
                     * Now it's not legal to return a heuristic from end, but
                     * apparently Oracle does (http://jira.jboss.com/jira/browse/JBTM-343)
                     * Since this is 1PC we can call forget: the outcome of the
                     * transaction is the outcome of the participant.
                     */

                    switch (e1.errorCode) {
                        case XAException.XA_HEURHAZ:
                        case XAException.XA_HEURMIX:
                        case XAException.XA_HEURCOM:
                        case XAException.XA_HEURRB:
                            endHeuristic = e1;
                            break;
                        case XAException.XA_RBROLLBACK:
                        case XAException.XA_RBCOMMFAIL:
                        case XAException.XA_RBDEADLOCK:
                        case XAException.XA_RBINTEGRITY:
                        case XAException.XA_RBOTHER:
                        case XAException.XA_RBPROTO:
                        case XAException.XA_RBTIMEOUT:
                        case XAException.XA_RBTRANSIENT:

                            /*
                             * Has been marked as rollback-only. We still
                             * need to call rollback.
                             */

                            endRBOnly = e1;
                            commit = false;
                            break;
                        case XAException.XAER_RMERR:
                        case XAException.XAER_NOTA:
                        case XAException.XAER_PROTO:
                        case XAException.XAER_INVAL:
                        case XAException.XAER_RMFAIL:
                        default: {
                            addDeferredThrowable(e1);

                            jtaLogger.i18NLogger.warn_resources_arjunacore_opcerror(XAHelper.xidToString(_tranID),
                                    _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

                            removeConnection();

                            return TwoPhaseOutcome.ONE_PHASE_ERROR;
                        }
                    }
                } catch (RuntimeException e) {
                    jtaLogger.i18NLogger.warn_resources_arjunacore_opcerror(XAHelper.xidToString(_tranID),
                            _theXAResource.toString(), "-", e);

                    throw e;
                }

                try {

                    /*
                     * Not strictly necessary since calling commit will
                     * do the rollback if end failed as above.
                     */

                    // catch those RMs that terminate in end rather than follow the spec
                    if (endHeuristic != null)
                        throw endHeuristic;

                    if (commit)
                        _theXAResource.commit(_tranID, true);
                    else {
                        _theXAResource.rollback(_tranID);
                        // _rolledBack set to true even though
                        // it's not needed at this stage
                        this._rolledBack = true;
                        throw endRBOnly;
                    }
                } catch (XAException e1) {
                    addDeferredThrowable(e1);

                    jtaLogger.i18NLogger.warn_resources_arjunacore_opcerror(XAHelper.xidToString(_tranID),
                            _theXAResource.toString(), XAHelper.printXAErrorCode(e1), e1);

                    /*
                     * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
                     * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
                     * XAER_PROTO. XA_RB*
                     */

                    switch (e1.errorCode) {
                        case XAException.XA_HEURHAZ:
                        case XAException.XA_HEURMIX:
                            _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                        case XAException.XA_HEURCOM:
                            forget();
                            break;
                        case XAException.XA_HEURRB:
                            _rolledBack = true;
                            forget();
                            return TwoPhaseOutcome.ONE_PHASE_ERROR;
                        case XAException.XA_RBROLLBACK:
                        case XAException.XA_RBCOMMFAIL:
                        case XAException.XA_RBDEADLOCK:
                        case XAException.XA_RBINTEGRITY:
                        case XAException.XA_RBOTHER:
                        case XAException.XA_RBPROTO:
                        case XAException.XA_RBTIMEOUT:
                        case XAException.XA_RBTRANSIENT:
                        case XAException.XAER_RMERR:

                            /* These codes imply that the RM rolled back the branch
                             * Setting _rolledBack to true is not strictly needed here,
                             * but we'll set it anyway for consistency
                             */

                            this._rolledBack = true;
                            return TwoPhaseOutcome.ONE_PHASE_ERROR;
                        case XAException.XAER_NOTA:
                            _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                            // something committed or rolled back without asking us!
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;

                        /*
                         * Some RMs do (or did) one-phase commit but interpreting end as prepare and
                         * once you’ve prepared (in end) you can commit or rollback when a timeout goes off
                         * I *think* we’re talking about a while ago so those RMs may no longer exist.
                         * The alternative implication is that the RM timed out the branch between
                         * the end above and the completion call, if we do make a change to assume that scenario
                         * it is possible we could break existing deployments so changes should be considered and
                         * potentially configurable
                         */

                        // resource manager failed, did it rollback?
                        case XAException.XAER_INVAL:
                            _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                        // XA does not allow this to be thrown for 1PC!
                        case XAException.XA_RETRY:
                        case XAException.XAER_PROTO:
                            this._rolledBack = true;
                            // assume rollback
                            return TwoPhaseOutcome.ONE_PHASE_ERROR;
                        // This was modified as part of JBTM-XYZ - although RMFAIL is not clear there is a rollback/commit we are flagging this to the user
                        case XAException.XAER_RMFAIL:
                            _heuristic = TwoPhaseOutcome.HEURISTIC_HAZARD;
                            return TwoPhaseOutcome.HEURISTIC_HAZARD;
                        default:
                            // will cause log to be rewritten
                            _committed = true;
                            // recovery should retry
                            return TwoPhaseOutcome.FINISH_ERROR;
                    }
                } catch (Exception e2) {
                    jtaLogger.i18NLogger.warn_resources_arjunacore_opcerror(XAHelper.xidToString(_tranID),
                            _theXAResource.toString(), "-", e2);

                    return TwoPhaseOutcome.FINISH_ERROR;
                } finally {
                    removeConnection();
                }
            } else {
                this._rolledBack = true;
                return TwoPhaseOutcome.ONE_PHASE_ERROR;
            }
        }

        if (commit)
            return TwoPhaseOutcome.FINISH_OK;
        else
            return TwoPhaseOutcome.FINISH_ERROR;
    }

    @Override
    public void clearHeuristicDecision() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.tracef("XAResourceRecord.clearHeuristicDecisition for %s changing from %d to %d",
                    this, _heuristic, TwoPhaseOutcome.FINISH_OK);
        }
        _heuristic = TwoPhaseOutcome.FINISH_OK;
    }

    public boolean forgetHeuristic() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.forget for " + this);
        }

        forget();

        // remove the connection regardless of whether or not the forget operation failed
        removeConnection();

        return _forgotten;
    }

    private void forget() {
        if ((_theXAResource != null) && (_tranID != null)) {
            try {
                _theXAResource.forget(_tranID);
                // only update the heuristic state if forget succeeded
                _heuristic = TwoPhaseOutcome.FINISH_OK;
                _forgotten = true;
            } catch (Exception e) {
                jtaLogger.i18NLogger.warn_recovery_forgetfailed(
                        "XAResourceRecord forget failed:", e);
                _forgotten = false;
            }
        }
    }

    /*
     * Independant recovery cannot occur. Must be driven by the recovery of the
     * local transaction, i.e., top-down recovery.
     */

    protected int recover() {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("XAResourceRecord.recover");
        }

        if (_committed) {

            /*
             * A previous commit attempt failed, but we know the intention
             * was to commit. So let's try again.
             */

            if (topLevelCommit() == TwoPhaseOutcome.FINISH_OK)
                return XARecoveryResource.RECOVERED_OK;
            else
                return XARecoveryResource.FAILED_TO_RECOVER;
        } else
            return XARecoveryResource.WAITING_FOR_RECOVERY;
    }

    public boolean save_state(OutputObjectState os, int t) {
        boolean res = false;

        try {
            os.packInt(_heuristic);
            os.packBoolean(_committed);

            /*
             * Even though there's still a chance of crashing before saving _rolledBack,
             * it's still worth trying to reduce the chances of invoking rollback again
             * on the same XA resource
             */

            os.packBoolean(_rolledBack);

            /*
             * Since we don't know what type of Xid we are using, leave it up to
             * XID to pack.
             */

            XidImple.pack(os, _tranID);

            /*
             * If no recovery object set then rely upon object serialisation!
             */

            if (_recoveryObject == null) {
                os.packInt(RecoverableXAConnection.OBJECT_RECOVERY);

                os.packString(_productName);
                os.packString(_productVersion);
                os.packString(_jndiName);

                if (_theXAResource instanceof Serializable) {
                    try {
                        ByteArrayOutputStream s = new ByteArrayOutputStream();
                        ObjectOutputStream o = new ObjectOutputStream(s);

                        o.writeObject(_theXAResource);
                        o.close();

                        os.packBoolean(true);
                        String name = _theXAResource.getClass().getName();
                        os.packString(name);

                        os.packBytes(s.toByteArray());
                    } catch (NotSerializableException ex) {
                        jtaLogger.i18NLogger.warn_resources_arjunacore_savestate();

                        return false;
                    }
                } else {
                    // have to rely upon XAResource.recover!

                    os.packBoolean(false);
                }
            } else {
                os.packInt(RecoverableXAConnection.AUTO_RECOVERY);
                os.packString(_recoveryObject.getClass().getName());

                _recoveryObject.packInto(os);
            }

            res = true;
        } catch (Exception e) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_savestateerror(_theXAResource.toString(), XAHelper.xidToString(_tranID), e);

            res = false;
        }

        if (res)
            res = super.save_state(os, t);

        return res;
    }

    public boolean restore_state(InputObjectState os, int t) {
        boolean res = false;

        try {
            _heuristic = os.unpackInt();
            _committed = os.unpackBoolean();
            _rolledBack = os.unpackBoolean();

            _tranID = XidImple.unpack(os);

            _theXAResource = null;
            _recoveryObject = null;

            if (os.unpackInt() == RecoverableXAConnection.OBJECT_RECOVERY) {
                _productName = os.unpackString();
                _productVersion = os.unpackString();
                _jndiName = os.unpackString();

                boolean haveXAResource = os.unpackBoolean();

                if (haveXAResource) {
                    try {
                        // Read the classname of the serialized XAResource
                        String className = os.unpackString();

                        byte[] b = os.unpackBytes();

                        ByteArrayInputStream s = new ByteArrayInputStream(b);
                        ObjectInputStream o = new ObjectInputStream(s);

                        // Give the list of deserializers a chance to deserialize the record
                        boolean deserialized = false;
                        Iterator<SerializableXAResourceDeserializer> iterator = getXAResourceDeserializers().iterator();
                        while (iterator.hasNext()) {
                            SerializableXAResourceDeserializer proxyXAResourceDeserializer = iterator.next();
                            if (proxyXAResourceDeserializer.canDeserialze(className)) {
                                _theXAResource = proxyXAResourceDeserializer.deserialze(o);
                                deserialized = true;
                                break;
                            }
                        }

                        // Give it a go ourselves
                        if (!deserialized) {
                            try {
                                _theXAResource = (XAResource) o.readObject();
                                if (jtaLogger.logger.isTraceEnabled()) {
                                    jtaLogger.logger.trace("XAResourceRecord.restore_state - XAResource de-serialized");
                                }
                            } catch (ClassNotFoundException e) {
                                // JBTM-2550 if we fail to deserialize the object, we treat it as haveXAResource is false
                                jtaLogger.i18NLogger.warn_resources_arjunacore_classnotfound(className);
                                haveXAResource = false;
                            }
                        }
                        o.close();
                    } catch (Exception ex) {
                        // not serializable in the first place!

                        jtaLogger.i18NLogger.warn_resources_arjunacore_restorestate(ex);

                        return false;
                    }
                }

                if (!haveXAResource) {

                    /*
                     * Lookup new XAResource via XARecoveryModule if possible.
                     */

                    _theXAResource = getNewXAResource();

                    if (_theXAResource == null) {
                        jtaLogger.i18NLogger.warn_resources_arjunacore_norecoveryxa(toString());

                        /*
                         * Don't prevent tx from activating because there may be
                         * other participants that can still recover. Plus, we will
                         * try to get a new XAResource later for this instance.
                         */

                        res = true;
                    }
                }
            } else {
                String creatorName = os.unpackString();

                _recoveryObject = ClassloadingUtility.loadAndInstantiateClass(RecoverableXAConnection.class, creatorName, null);
                if (_recoveryObject == null) {
                    throw new ClassNotFoundException();
                }

                _recoveryObject.unpackFrom(os);
                _theXAResource = _recoveryObject.getResource();

                if (jtaLogger.logger.isTraceEnabled()) {
                    jtaLogger.logger.trace("XAResourceRecord.restore_state - XAResource got from "
                            + creatorName);
                }
            }

            res = true;
        } catch (Exception e) {
            jtaLogger.i18NLogger.warn_resources_arjunacore_restorestateerror(_theXAResource.toString(), XAHelper.xidToString(_tranID), e);

            res = false;
        } finally {

            if (res)
                res = super.restore_state(os, t);

            /*
             * If we're here then we've restored enough to print data on
             * this instance.
             */

            if (_heuristic != TwoPhaseOutcome.FINISH_OK) {
                jtaLogger.logger.warn("XAResourceRecord restored heuristic instance: " + this);
            }
        }

        return res;
    }

    public String type() {
        return XAResourceRecord.typeName();
    }

    public static String typeName() {
        return "/StateManager/AbstractRecord/XAResourceRecord";
    }

    public boolean doSave() {
        return true;
    }

    public void merge(AbstractRecord a) {
    }

    public void alter(AbstractRecord a) {
    }

    public boolean shouldAdd(AbstractRecord a) {
        return false;
    }

    public boolean shouldAlter(AbstractRecord a) {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a) {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a) {
        return false;
    }

    /**
     * Returns the resource manager product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return _productName;
    }

    /**
     * Sets the resource manager product name.
     *
     * @param productName the product name
     */
    public void setProductName(String productName) {
        this._productName = productName;
    }

    /**
     * Returns the resource manager product version.
     *
     * @return the product version
     */
    public String getProductVersion() {
        return _productVersion;
    }

    /**
     * Sets the resource manager product version.
     *
     * @param productVersion the product version
     */
    public void setProductVersion(String productVersion) {
        this._productVersion = productVersion;
    }

    /**
     * Returns the resource manager JNDI name for e.g. xa datasource.
     * Note this is not used for lookup, only for information.
     *
     * @return the JNDI name.
     */
    public String getJndiName() {
        return _jndiName;
    }

    /**
     * Sets the resource manager JNDI name.
     * Note this is not used for lookup, only for information.
     *
     * @param jndiName the JNDI name.
     */
    public void setJndiName(String jndiName) {
        this._jndiName = jndiName;
    }

    public XAResourceRecord() {
        super();

        _theXAResource = null;
        _recoveryObject = null;
        _tranID = null;
        _prepared = true;
        _heuristic = TwoPhaseOutcome.FINISH_OK;
        _valid = true;
        _theTransaction = null;
        _recovered = true;

    }

    public XAResourceRecord(Uid u) {
        super(u, null, ObjectType.ANDPERSISTENT);

        _theXAResource = null;
        _recoveryObject = null;
        _tranID = null;
        _prepared = true;
        _heuristic = TwoPhaseOutcome.FINISH_OK;
        _valid = true;
        _theTransaction = null;
        _recovered = true;
    }

    public String toString() {
        return "XAResourceRecord < resource:" + _theXAResource + ", txid:" + _tranID +
                ", heuristic: " + TwoPhaseOutcome.stringForm(_heuristic) +
                ((_productName != null && _productVersion != null) ? ", product: " + _productName + "/" + _productVersion : "") +
                ((_jndiName != null) ? ", jndiName: " + _jndiName : "") +
                " " + super.toString() + " >";
    }

    private List<SerializableXAResourceDeserializer> getXAResourceDeserializers() {
        if (serializableXAResourceDeserializers != null) {
            return serializableXAResourceDeserializers;
        }
        synchronized (this) {
            if (serializableXAResourceDeserializers != null) {
                return serializableXAResourceDeserializers;
            }
            serializableXAResourceDeserializers = new ArrayList<SerializableXAResourceDeserializer>();
            for (RecoveryModule recoveryModule : RecoveryManager.manager().getModules()) {
                if (recoveryModule instanceof XARecoveryModule) {
                    XARecoveryModule xaRecoveryModule = (XARecoveryModule) recoveryModule;
                    serializableXAResourceDeserializers.addAll(xaRecoveryModule.getSeriablizableXAResourceDeserializers());
                    return serializableXAResourceDeserializers;
                }
            }

        }
        return serializableXAResourceDeserializers;
    }

    /**
     * This routine finds the new XAResource for the transaction that used the
     * old resource we couldn't serialize. It does this by looking up the
     * XARecoveryModule in the recovery manager and asking it for the
     * XAResource. The recovery manager will then look through its list of
     * registered XARecoveryResource implementations for the appropriate
     * instance. If the XARecoveryModule hasn't been initialised yet then this
     * routine will fail, but on the next scan it should work.
     */

    private final XAResource getNewXAResource() {
        RecoveryManager recMan = RecoveryManager.manager();
        Vector recoveryModules = recMan.getModules();

        if (recoveryModules != null) {
            Enumeration modules = recoveryModules.elements();

            while (modules.hasMoreElements()) {
                RecoveryModule m = (RecoveryModule) modules.nextElement();

                if (m instanceof XARecoveryModule) {

                    /*
                     * Blaargh! There are better ways to do this!
                     */

                    return ((XARecoveryModule) m).getNewXAResource(this);
                }
            }
        }

        return null;
    }

    private final void removeConnection() {

        /*
         * Should only be called once. Remove the connection so that user can
         * reuse the driver as though it were fresh (e.g., can do read only
         * optimisation).
         */

        if (_recoveryObject != null) {
            _recoveryObject.close();
        }

        if (_theTransaction != null)
            _theTransaction = null;
    }

    /*
     * Ask the transaction whether or not this XAResource is still associated
     * with the thread, i.e., has end already been called on it?
     */

    private void endAssociation(int xaState, int txInfoState) throws XAException {
        if (_theTransaction != null) {
            _theTransaction.endAssociation(_tranID, _theXAResource, xaState, txInfoState);
        }
    }

    private boolean wasResourceContactedByRecoveryModule(final String jndiName) {
        final Vector<RecoveryModule> recoveryModules = RecoveryManager.manager().getModules();

        for (final RecoveryModule recoveryModule : recoveryModules) {
            if (recoveryModule instanceof XARecoveryModule) {
                return ((XARecoveryModule) recoveryModule).getContactedJndiNames().contains(jndiName);
            }
        }

        return false;
    }

    public int getHeuristic() {
        return _heuristic;
    }

    public boolean isForgotten() {
        return _forgotten;
    }

    protected XAResource _theXAResource;
    private boolean _forgotten;

    private RecoverableXAConnection _recoveryObject;
    private Xid _tranID;

    private boolean _prepared;

    private boolean _valid;

    private int _heuristic;

    // try to optimize recovery
    private boolean _committed = false;

    private TransactionImple _theTransaction;
    private boolean _recovered = false;
    private boolean _rolledBack = false;

    // extra metadata from the wrapper, if present
    private String _productName;
    private String _productVersion;
    private String _jndiName;
    private static final XAResourceRecordWrappingPlugin _xaResourceRecordWrappingPlugin =
            jtaPropertyManager.getJTAEnvironmentBean().getXAResourceRecordWrappingPlugin();

    /*
     * WARNING: USE WITH EXTEREME CARE!!
     *
     * This assumes that if there is no XAResource that can deal with an Xid
     * after recovery, then we failed after successfully committing the transaction
     * but before updating the log. In which case we just need to ignore this
     * resource and remove the entry from the log.
     *
     * BUT if not all XAResourceRecovery instances are correctly implemented
     * (or present) we may end up removing participants that have not been dealt
     * with. Hence USE WITH EXTREME CARE!!
     */
    private static final boolean _assumedComplete = jtaPropertyManager.getJTAEnvironmentBean().isXaAssumeRecoveryComplete();

    private List<SerializableXAResourceDeserializer> serializableXAResourceDeserializers;

    void addDeferredThrowable(Exception e) {
        if (this.deferredExceptions == null)
            this.deferredExceptions = new ArrayList<>();
        this.deferredExceptions.add(e);
    }

    @Override
    public void getDeferredThrowables(List<Throwable> list) {
        if (deferredExceptions != null)
            list.addAll(deferredExceptions);
    }

}