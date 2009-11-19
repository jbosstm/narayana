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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceRecordViewer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;

import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicCommit;

/**
 * This is a state viewer for a JTS ResourceRecord.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: ResourceRecordViewer.java 2342 2006-03-30 13:06:17Z  $
 */
public class ResourceRecordViewer extends AbstractRecordViewer
{
    private final static String ORB_NAME = "tools-orb";

    private ORB _orb = null;

    /**
     * When this viewer is created we need to ensure that the ORB is initialised.
     * @throws Exception e 
     */
    public ResourceRecordViewer() throws Exception
    {
        try
        {
            if (!ORBManager.isInitialised())
            {
                _orb = ORB.getInstance(ORB_NAME);
                OA oa = OA.getRootOA(_orb);

                _orb.initORB((String[])null, null);
                oa.initPOA(null);
            }

            Implementations.initialise();
        }
        catch (Exception e)
        {
            /** The ORB has failed to initialise don't allow this plugin to be used **/
            throw e;
        }
    }

    /**
     * An entry has been selected of the type this viewer is registered against.
     *
     * @param record
     * @param action
     * @param entry
     * @param statePanel
     * @throws ObjectStoreException
     */
    public void entrySelected(final AbstractRecord record,
                              final BasicAction action,
                              final ObjectStoreViewEntry entry,
                              final StatePanel statePanel) throws ObjectStoreException
    {
        super.entrySelected(record, action, entry, statePanel);
    }

    protected void initRecord(BasicAction action, AbstractRecord record, ObjectStoreViewEntry entry)
    {
        super.initRecord(action, record, entry);
    }

    protected boolean doOp(ResourceRecord record, RecoveryOp op)
    {
        try
        {
            switch (op) {
                case FORGET_OP:
                    record.resourceHandle().forget();
                    break;
                case COMMIT_OP:
                    // commit 1PC since the user is bypassing the TM
                    record.resourceHandle().commit_one_phase();
                    break;
                case ABORT_OP:
                    record.resourceHandle().rollback();
                    break;
                default:
            }

            return true;
        }
        catch (HeuristicMixed e)
        {
            appendError("Heuristic mixed error: " + e.getMessage());
        }
        catch (HeuristicHazard e)
        {
            appendError("Heuristic hazard error: " + e.getMessage());
        }
        catch (HeuristicCommit e)
        {
            appendError("Heuristic commit error: " + e.getMessage());
        }
        catch (Throwable t)
        {
            appendError("Error: " + t.getMessage());
        }

        return false;
    }

    protected boolean doOp(ExtendedResourceRecord record, RecoveryOp op)
    {
        try
        {
            switch (op) {
                case FORGET_OP:
                    record.resourceHandle().forget();
                    break;
                case COMMIT_OP:
                    record.resourceHandle().commit_one_phase();
                    break;
                case ABORT_OP:
                    record.resourceHandle().rollback();
                    break;
                default:
            }

            return true;
        }
        catch (HeuristicMixed e)
        {
            appendError("Heuristic mixed error: " + e.getMessage());
        }
        catch (HeuristicHazard e)
        {
            appendError("Heuristic hazard error: " + e.getMessage());
        }
        catch (HeuristicCommit e)
        {
            appendError("Heuristic commit error: " + e.getMessage());
        }
        catch (Throwable t)
        {
            appendError("Error: " + t.getMessage());    
        }

        return false;
    }

    protected boolean doOp(XAResourceRecord record, RecoveryOp op)
    {
        try
        {
            switch (op) {
                case FORGET_OP:
                    ((XAResource)record.value()).forget(record.getXid());
                    break;
                case COMMIT_OP:
                    ((XAResource)record.value()).commit(record.getXid(), true);
                    break;
                case ABORT_OP:
                    ((XAResource)record.value()).rollback(record.getXid());
                    break;
                default:
            }

            return true;
        }
        catch (XAException e)
        {
            appendError("XA error: " + e.getMessage());
            return false;
        }
    }

    protected boolean doOp(AbstractRecord record, RecoveryOp op)
    {
        try
        {
            int err;

            switch (op) {
                case FORGET_OP:
                    if (!record.forgetHeuristic())
                    {
                        appendError("Could not forget this record");
                        return false;
                    }
                    return true;
                case COMMIT_OP:
                    err = record.topLevelOnePhaseCommit();

                    if (err == TwoPhaseOutcome.FINISH_OK)
                        return true;

                    appendError("Commit error: " + TwoPhaseOutcome.stringForm(err));
                    return false;
                case ABORT_OP:
                    err = record.topLevelAbort();

                    if (err == TwoPhaseOutcome.FINISH_OK)
                        return true;

                    appendError("Commit error: " + TwoPhaseOutcome.stringForm(err));
                    return false;
                default:
                    return true;
            }
        }
        catch (Exception e)
        {
            appendError("Unable to complete operation: " + e.getMessage());
            return false;
        }
    }

    protected boolean doOp(RecoveryOp op)
    {
        if (getRecord() instanceof ResourceRecord)
            return doOp((ResourceRecord) getRecord(), op);
        else if (getRecord() instanceof XAResourceRecord)
            return doOp((XAResourceRecord) getRecord(), op);
        else if (getRecord() instanceof ExtendedResourceRecord)
            return doOp((ExtendedResourceRecord) getRecord(), op);
        else
            appendError("Unsupported record type: " + getRecord().getClass().getSimpleName());

        return false;
    }

    protected boolean doForget()
    {
        return doOp(RecoveryOp.FORGET_OP);        
    }

    protected boolean doCommit()
    {
        return doOp(RecoveryOp.COMMIT_OP);
    }

    protected boolean doRollback()
    {
        return doOp(RecoveryOp.ABORT_OP);
    }

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType()
    {
        return "/StateManager/AbstractRecord/ResourceRecord";
    }
}
