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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: LastResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.abstractrecords;

import java.io.PrintWriter;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.OnePhaseResource;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * AbstractRecord that helps us do the last resource commit optimization.
 * Basically this is something that is used to allow a *single* resource that is
 * only one-phase aware to be enlisted with a transaction that is usually
 * two-phase. The way it works is: (i) the coordinator runs its normal first
 * (prepare) phase on all two-phase aware participants and makes a decision
 * based solely on their responses as to whether to commit or roll back. Note,
 * the one-phase aware resource essentially returns voteCommit during prepare,
 * to ensure that the second phase runs even if all other resources return
 * voteReadOnly. (ii) if the transaction is to commit then the coordinator
 * invokes the second phase on *all* participants, starting with the one that is
 * only one-phase aware. If it rolls back, it rolls all resources back, but the
 * order is not important.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LastResourceRecord.java 2342 2006-03-30 13:06:17Z $
 * @since ATS 3.2.
 */

public class LastResourceRecord extends AbstractRecord
{

    public LastResourceRecord(OnePhaseResource opr)
    {
        super(ONE_PHASE_RESOURCE_UID);

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord()");
        }

        _lro = opr;
    }

    public boolean propagateOnCommit ()
    {
        return false;
    }

    public int typeIs ()
    {
        return RecordType.LASTRESOURCE;
    }

    public int nestedAbort ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::nestedAbort() for " + order());
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::nestedCommit() for " + order());
        }

        return TwoPhaseOutcome.FINISH_ERROR;
    }

    /**
     * Not allowed to participate in nested transactions.
     */

    public int nestedPrepare ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::nestedPrepare() for " + order());
        }

        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::topLevelAbort() for " + order());
        }

        if (_lro != null)
        {
            return _lro.rollback();
        }
        else
        {
            return TwoPhaseOutcome.FINISH_OK;
        }
    }

    public int topLevelCommit ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::topLevelCommit() for " + order());
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::topLevelPrepare() for " + order());
        }

        if (_lro == null)
            return TwoPhaseOutcome.PREPARE_NOTOK;

        switch (_lro.commit())
        {
        case TwoPhaseOutcome.FINISH_OK:
            return TwoPhaseOutcome.PREPARE_OK;
        case TwoPhaseOutcome.ONE_PHASE_ERROR:
            return TwoPhaseOutcome.ONE_PHASE_ERROR;
        default:
            return TwoPhaseOutcome.PREPARE_NOTOK;
        }
    }

    public int topLevelOnePhaseCommit ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LastResourceRecord::topLevelOnePhase() for " + order());
        }

        if (_lro == null)
            return TwoPhaseOutcome.PREPARE_NOTOK;

        switch (_lro.commit())
        {
        case TwoPhaseOutcome.FINISH_OK:
            return TwoPhaseOutcome.FINISH_OK;
        case TwoPhaseOutcome.ONE_PHASE_ERROR:
            return TwoPhaseOutcome.ONE_PHASE_ERROR;
        default:
            // in case of error
            // FINISH_ERROR is used for 2PC there was an error and expecting recovery will retry (not available here)
            //  no exception thrown to the caller
            // ONE_PHASE_ERROR assuming rollback but we don't know the outcome (was rolled-back or committed in the RM)
            //  javax.transaction.RollbackException thrown to the caller
            // HEURISTIC_MIXED used to get javax.transaction.HeuristicMixedException to the caller
            return TwoPhaseOutcome.HEURISTIC_MIXED;
        }
    }

    @Override
    public String toString()
    {


        return "LastResourceRecord("+_lro+")";
    }

    public void print (PrintWriter strm)
    {
        strm.println("LastResource for:");
        super.print(strm);
    }

    public String type ()
    {
        return "/StateManager/AbstractRecord/LastResourceRecord";
    }

    public boolean shouldAdd (AbstractRecord a)
    {
        if (a.typeIs() == typeIs())
        {
            if (ALLOW_MULTIPLE_LAST_RESOURCES) {
                if (!_disableMLRWarning
                        || (_disableMLRWarning && !_issuedWarning)) {
                    tsLogger.i18NLogger.warn_lastResource_multipleWarning(a.toString());
                    _issuedWarning = true;
                }

                return true;
            }
            else {
                tsLogger.i18NLogger.warn_lastResource_disallow(this.toString(), a.toString());

                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean shouldMerge (AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace (AbstractRecord a)
    {
        return false;
    }

    public boolean shouldAlter (AbstractRecord a)
    {
        return false;
    }

    public void merge (AbstractRecord a)
    {
    }

    public void alter (AbstractRecord a)
    {
    }

    /**
     * @return <code>Object</code> to be used to order.
     */

    public Object value ()
    {
        return _lro;
    }

    public void setValue (Object o)
    {
    }

    public LastResourceRecord()
    {
        super();

        _lro = null;
    }

    private OnePhaseResource _lro;

    private static final Uid ONE_PHASE_RESOURCE_UID = Uid.lastResourceUid();

    private static final boolean ALLOW_MULTIPLE_LAST_RESOURCES = arjPropertyManager
            .getCoreEnvironmentBean().isAllowMultipleLastResources();

    private static final boolean _disableMLRWarning = arjPropertyManager.getCoreEnvironmentBean()
            .isDisableMultipleLastResourcesWarning();

    private static boolean _issuedWarning = false;

    /**
     * Static block writes warning messages to the log if either multiple last resources are enabled
     * or multiple last resources warning is disabled.
     */
    static
    {
        if (ALLOW_MULTIPLE_LAST_RESOURCES) {
            tsLogger.i18NLogger.warn_lastResource_startupWarning();
        }

        if (_disableMLRWarning) {
            tsLogger.i18NLogger.warn_lastResource_disableWarning();
        }
    }
}
