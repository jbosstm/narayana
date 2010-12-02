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
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.StoreManagerProxy;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * implementation of the JMX interface to the JBossTS participant store
 */
public class ParticipantStoreBean extends TxLogBean implements ParticipantStoreBeanMBean {

	private ParticipantStore ps;

    /**
     * Construct an MBean corresponding to the default participant store in this JVM
     */
	public ParticipantStoreBean() {
        super(StoreManager.getParticipantStore());
        ps = (ParticipantStore) getStore();
	}

    /**
     * Construct an MBean corresponding to the given store
     * @param ps the ParticipantStore that is wrapped by this MBean
     */
    public ParticipantStoreBean(ParticipantStore ps) {
        super(ps);
        this.ps = ps;
    }

    @Override
    protected ObjectName getMBeanName() {
        try {
            return new ObjectName(StoreManagerProxy.PARTICIPANT_BEAN_NAME);
        } catch (MalformedObjectNameException e) {
            System.out.println("Error creating object name: " + e.getMessage());
            return null;
        }
    }

	// ParticipantStore interface implementation

	public boolean commit_state (Uid u, String tn) throws ObjectStoreException {
		return ps.commit_state (u, tn);
	}
    
	public ObjectStateWrapper read_committed (Uid u, String tn) throws ObjectStoreException {
		InputObjectState ios = ps.read_committed (u, tn);
        return new ObjectStateWrapper(ios);
	}

	public ObjectStateWrapper read_uncommitted (Uid u, String tn) throws ObjectStoreException {
		InputObjectState ios = ps.read_uncommitted (u, tn);
        return new ObjectStateWrapper(ios);
	}

	public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException {
		return ps.remove_uncommitted (u, tn);
	}

	public boolean write_uncommitted (Uid u, String tn, OutputObjectStateWrapper buff) throws ObjectStoreException {
		return ps.write_uncommitted (u, tn, buff.getOOS());
	}

	public boolean fullCommitNeeded () {
		return ps.fullCommitNeeded ();
	}
}
