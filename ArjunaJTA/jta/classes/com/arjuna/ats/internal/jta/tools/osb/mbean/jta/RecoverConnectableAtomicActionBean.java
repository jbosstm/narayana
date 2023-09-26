/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBeanWrapperInterface;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoverConnectableAtomicAction;

import java.io.IOException;

/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class RecoverConnectableAtomicActionBean extends JTAActionBean implements RecoverConnectableAtomicActionBeanMBean {
    RecoverConnectableAtomicAction connectableAtomicAction = null;

    public RecoverConnectableAtomicActionBean(UidWrapper w) {
        super(w);
    }

    @Override
    protected ActionBeanWrapperInterface createWrapper(UidWrapper w, boolean activate) {
        try {
            InputObjectState state = StoreManager.getRecoveryStore().read_committed(w.getUid(), RecoverConnectableAtomicAction.CONNECTABLE_ATOMIC_ACTION_TYPE);
            connectableAtomicAction = new RecoverConnectableAtomicAction(RecoverConnectableAtomicAction.CONNECTABLE_ATOMIC_ACTION_TYPE, w.getUid(), state);

        } catch (ObjectStoreException e) {
            ;
        } catch (IOException e) {
            ;
        }

        GenericAtomicActionWrapper action = new GenericAtomicActionWrapper(connectableAtomicAction, w);

        if (activate)
            action.activate();

        return action;
    }

    @Override
    public String toDo() {
        return "TODO RecoverConnectableAtomicActionBean";
    }
}