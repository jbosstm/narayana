/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Coordinator;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerNestedAction;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerStrictNestedAction extends ServerNestedAction
{

    /*
     * Create local transactions with same ids as remote. The base class is
     * responsible for registering us with the parent transaction.
     */

    public ServerStrictNestedAction(ServerControl control, boolean doRegister)
    {
        super(control);

        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerStrictNestedAction::ServerStrictNestedAction ( "
                    + _theUid + " )");
        }

        _registered = false;
        _theResource = null;

        if (_theControl != null)
        {
            _theResource = new org.omg.CosTransactions.SubtransactionAwareResourcePOATie(
                    this);

            ORBManager.getPOA().objectIsReady(_theResource);

            /*
             * Would like to be able to attach a thread filter to this object if
             * process-filters aren't supported. However, currently this won't
             * work as we can't have two different filter types working at the
             * same time. ATTACH_THREAD_FILTER_(_theResource);
             */

            if (doRegister)
                interposeResource();
        }
    }

    public boolean interposeResource ()
    {
        if (!_registered)
        {
            _registered = true;

            if ((_theResource != null) && (_theControl != null))
            {
                Coordinator realCoordinator = _theControl.originalCoordinator();

                if (!(_valid = registerSubTran(realCoordinator))) {
                    jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_strict_ipfailed("ServerStrictNestedAction");

                    /*
                     * Failed to register. Valid is set, and the interposition
                     * controller will now deal with this.
                     */
                }

                realCoordinator = null;
            }
            else
                _valid = false;
        }

        return _valid;
    }

    /*
     * Since we may be called multiple times if we are nested and are propagated
     * to our parents, we remember the initial response and return it
     * subsequently.
     */

    public void commit_subtransaction (Coordinator parent)
            throws SystemException
    {
        if (jtsLogger.logger.isTraceEnabled()) {
            jtsLogger.logger.trace("ServerStrictNestedAction::commit_subtransaction : "
                    + _theUid);
        }

        try
        {
            super.commit_subtransaction(parent);

            /*
             * Now register a resource with our parent if required.
             */

            if (super._parent != null)
                super._parent.interposeResource();
        }
        catch (SystemException e)
        {
            if (super._parent != null)
                super._parent.interposeResource();

            throw e;
        }
    }

    private boolean _registered;

}