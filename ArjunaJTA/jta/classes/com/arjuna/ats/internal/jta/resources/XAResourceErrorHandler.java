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
 * Arjuna Technologies Ltd, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: XAResourceErrorHandler.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources;

import java.util.HashMap;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.resources.XAResourceMap;
import com.arjuna.ats.jta.utils.XAHelper;

public class XAResourceErrorHandler
{
    private XAException e;
    private XAResource xaResource;
    private Xid xid;
    private boolean committed = false;

    public XAResourceErrorHandler(XAException e, XAResource xaResource, Xid xid) {
        this.xid = xid;
        this.xaResource = xaResource;
        this.e = e;
    }

    /**
     * Is the XAException a non-error when received in reply to commit or
     * rollback ? It normally is, but may be overridden in recovery.
     */
    protected boolean notAProblem(boolean commit)
    {
        return XAResourceErrorHandler.notAProblem(xaResource, e, commit);
    }

    public int handleCMRRollbackError() {
        if (notAProblem(false))
        {
            // some other thread got there first (probably)
        }
        else
        {
// TODO			addDeferredThrowable(e);

            jtaLogger.i18NLogger.warn_resources_arjunacore_rollbackerror(XAHelper.xidToString(xid),
                    xaResource.toString(), XAHelper.printXAErrorCode(e), e);

            switch (e.errorCode)
            {
                case XAException.XAER_RMERR:
                    break; // just do the finally block
                case XAException.XA_HEURHAZ:
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                case XAException.XA_HEURCOM:
                    return TwoPhaseOutcome.HEURISTIC_COMMIT;
                case XAException.XA_HEURMIX:
                    return TwoPhaseOutcome.HEURISTIC_MIXED;
                case XAException.XAER_NOTA:
//                    if (_recovered)
                    // assume it has already rolled back
                    break; // rolled back previously and recovery completed
                case XAException.XA_HEURRB: // forget?
                case XAException.XA_RBROLLBACK:
                case XAException.XA_RBEND:
                case XAException.XA_RBCOMMFAIL:
                case XAException.XA_RBDEADLOCK:
                case XAException.XA_RBINTEGRITY:
                case XAException.XA_RBOTHER:
                case XAException.XA_RBPROTO:
                case XAException.XA_RBTIMEOUT:
                    break;
                default:
                    return TwoPhaseOutcome.FINISH_ERROR;
            }
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public void forget() {

        // only relevant for two phase aware resources
/*        try {
            xaResource.forget(xid);
        } catch (XAException e) {
            // ignore
        }*/
    }

    public int handleCMRCommitError(boolean onePhase) {
//TODO        addDeferredThrowable(e1);

        jtaLogger.i18NLogger.warn_resources_arjunacore_opcerror(XAHelper.xidToString(xid),
                xaResource.toString(), XAHelper.printXAErrorCode(e), e);

        if (onePhase) {
            switch (e.errorCode)
            {
                case XAException.XA_HEURHAZ:
                case XAException.XA_HEURMIX:
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                case XAException.XA_HEURCOM:
                    forget();
                    break;
                case XAException.XA_HEURRB:
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
                    return TwoPhaseOutcome.ONE_PHASE_ERROR;
                case XAException.XAER_NOTA:
                    return TwoPhaseOutcome.HEURISTIC_HAZARD; // something committed or rolled back without asking us!
                case XAException.XAER_INVAL: // resource manager failed, did it rollback?
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                case XAException.XA_RETRY:  // XA does not allow this to be thrown for 1PC!
                case XAException.XAER_PROTO:
                    return TwoPhaseOutcome.ONE_PHASE_ERROR; // assume rollback
                case XAException.XAER_RMFAIL:
                default:
                    committed = true;
                    return TwoPhaseOutcome.FINISH_ERROR;  // ? recovery should retry
            }

            return TwoPhaseOutcome.FINISH_OK;
        } else {
            switch (e.errorCode)
            {
                case XAException.XA_HEURHAZ:
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
                case XAException.XA_HEURCOM: // what about forget?
                    // OTS doesn't support
                    // this code here.
                    break;
                case XAException.XA_HEURRB:
                case XAException.XA_RBROLLBACK:  // could really do with an ABORTED status in TwoPhaseOutcome to differentiate
                case XAException.XA_RBCOMMFAIL:
                case XAException.XA_RBDEADLOCK:
                case XAException.XA_RBINTEGRITY:
                case XAException.XA_RBOTHER:
                case XAException.XA_RBPROTO:
                case XAException.XA_RBTIMEOUT:
                case XAException.XA_RBTRANSIENT:
                case XAException.XAER_RMERR:
                case XAException.XAER_PROTO:  // XA spec implies rollback
                    return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
                case XAException.XA_HEURMIX:
                    return TwoPhaseOutcome.HEURISTIC_MIXED;
                case XAException.XAER_NOTA:
//                    if (_recovered)
//                        break; // committed previously and recovery completed
//                    else
                        return TwoPhaseOutcome.HEURISTIC_HAZARD;  // something terminated the transaction!
                case XAException.XA_RETRY:
                case XAException.XAER_RMFAIL:
                    committed = true;  // will cause log to be rewritten

	                /*
                     * Could do timeout retry here, but that could cause other resources in the list to go down the
                     * heuristic path (some are far too keen to do this). Fail and let recovery retry. Meanwhile
                     * the coordinator will continue to commit the other resources immediately.
                     */
                    return TwoPhaseOutcome.FINISH_ERROR;
                case XAException.XAER_INVAL: // resource manager failed, did it rollback?
                default:
                    return TwoPhaseOutcome.HEURISTIC_HAZARD;
            }

            return TwoPhaseOutcome.FINISH_OK;
        }
    }

    public boolean isCommitted() {
        return committed;
    }

	public static boolean notAProblem (XAResource res, XAException ex, boolean commit)
	{
		boolean isNotAProblem = false;
		XAResourceMap theMap = _maps.get(res.getClass().getName());

		if (theMap != null)
			isNotAProblem = theMap.notAProblem(ex, commit);

		return isNotAProblem;
	}

	public static void addXAResourceMap (String type, XAResourceMap map)
	{
		_maps.put(type, map);
	}

	private static HashMap<String, XAResourceMap> _maps = new HashMap<String, XAResourceMap> ();

    /**
     * Static block puts all XAResourceMap instances defined in JTAEnvironmentBean to the XAResourceErrorHandler's hash map.
     * They are later used to check if the XAException is a non-error when received in reply to commit or rollback.
     */
    static
    {
        for(XAResourceMap xaResourceMap : jtaPropertyManager.getJTAEnvironmentBean().getXaResourceMaps())
        {
            XAResourceErrorHandler.addXAResourceMap(xaResourceMap.getXAResourceName(), xaResourceMap);
        }
    }
}
