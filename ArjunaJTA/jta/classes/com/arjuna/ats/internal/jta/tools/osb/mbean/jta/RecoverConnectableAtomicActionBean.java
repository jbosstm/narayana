/*
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2014
 * @author JBoss Inc.
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
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
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
