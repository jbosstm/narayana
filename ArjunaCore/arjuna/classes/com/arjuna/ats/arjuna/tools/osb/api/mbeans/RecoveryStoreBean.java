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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.StoreManagerProxy;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * implementation of the JMX interface to the JBossTS recovery store
 */
public class RecoveryStoreBean extends TxLogBean implements RecoveryStoreBeanMBean {
	private RecoveryStore rs;

    /**
     * Construct an MBean corresponding to the default recovery store in this JVM
     */
	public RecoveryStoreBean() {
        super(StoreManager.getRecoveryStore());
		rs = (RecoveryStore) getStore();
	}
    /**
     * Construct an MBean corresponding to the given store
     * @param rs the RecoveryStore that is wrapped by this MBean
     */
    public RecoveryStoreBean(RecoveryStore rs) {
        super(rs);
        this.rs = rs;
    }

    @Override
    protected ObjectName getMBeanName() {
        try {
            return new ObjectName(StoreManagerProxy.RECOVERY_BEAN_NAME);
        } catch (MalformedObjectNameException e) {
            System.out.println("Error creating object name: " + e.getMessage());
            return null;
        }
    }

	// RecoveryStore interface implementation

	public ObjectStateWrapper allObjUids(String type, int m) throws ObjectStoreException {
        InputObjectState ios = new InputObjectState();
		boolean ok = rs.allObjUids (type, ios, m);
        return new ObjectStateWrapper(ios, ok);
	}

	public ObjectStateWrapper allObjUids(String type) throws ObjectStoreException {
        InputObjectState ios = new InputObjectState();
		boolean ok = rs.allObjUids (type, ios);
        return new ObjectStateWrapper(ios, ok);
	}

	public ObjectStateWrapper allTypes() throws ObjectStoreException {
        InputObjectState ios = new InputObjectState();
        boolean ok = rs.allTypes(ios);
		return new ObjectStateWrapper(ios, ok);
	}

	public int currentState (Uid u, String tn) throws ObjectStoreException {
		return rs.currentState (u, tn);
	}

	public boolean hide_state (Uid u, String tn) throws ObjectStoreException {
		return rs.hide_state (u, tn);
	}

	public boolean reveal_state (Uid u, String tn) throws ObjectStoreException {
		return rs.reveal_state (u, tn);
	}

	public ObjectStateWrapper read_committed (Uid u, String tn) throws ObjectStoreException {
		InputObjectState ios = rs.read_committed (u, tn);
        return new ObjectStateWrapper(ios);
	}

	public boolean isType (Uid u, String tn, int st) throws ObjectStoreException {
		return rs.isType (u, tn, st);
	}
}
