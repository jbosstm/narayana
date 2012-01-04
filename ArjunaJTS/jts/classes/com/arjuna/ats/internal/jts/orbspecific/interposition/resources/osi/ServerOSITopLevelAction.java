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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerOSITopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;

import com.arjuna.ats.internal.jts.interposition.resources.osi.OTIDMap;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict.ServerStrictTopLevelAction;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerOSITopLevelAction extends ServerStrictTopLevelAction
{

    /*
     * The ServerTopLevelAction is responsible for registering this resource
     * with its parent.
     */

    public ServerOSITopLevelAction(ServerControl control, boolean doRegister)
    {
        super(control, doRegister);

        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::ServerOSITopLevelAction ( ServerControl, "
                    + doRegister + " )");
        }
    }

    /*
     * Will only be called by the remote top-level transaction.
     */

    public org.omg.CosTransactions.Vote prepare () throws HeuristicMixed,
            HeuristicHazard, SystemException
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::prepare for " + _theUid);
        }

        /*
         * First remove entry for this transaction otid from map. Have to do it
         * here as we are going to be deleted by the base class!
         */

        OTIDMap.remove(get_uid());

        return super.prepare();
    }

    public void rollback () throws SystemException, HeuristicCommit,
            HeuristicMixed, HeuristicHazard
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::rollback for " + _theUid);
        }

        OTIDMap.remove(get_uid());

        super.rollback();
    }

    public void commit () throws SystemException, NotPrepared,
            HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::commit for " + _theUid);
        }

        OTIDMap.remove(get_uid());

        super.commit();
    }

    public void forget () throws SystemException
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::forget for " + _theUid);
        }

        OTIDMap.remove(get_uid());

        super.forget();
    }

    /*
     * Just because commit_one_phase is called by the coordinator does not mean
     * that we can use it - we may have many locally registered resources.
     */

    public void commit_one_phase () throws HeuristicHazard, SystemException
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerOSITopLevelAction::commit_one_phase for " + _theUid);
        }

        OTIDMap.remove(get_uid());

        super.commit_one_phase();
    }

    public String type ()
    {
        return "/Resources/Arjuna/ServerTopLevelAction/ServerOSITopLevelAction";
    }

}
