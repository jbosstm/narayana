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
package com.arjuna.ats.arjuna.tools.osb.api.proxy;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.OutputObjectStateWrapper;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.ParticipantStoreBeanMBean;

/**
 * Remote proxy to a ParticipantStore
 */
public class ParticipantStoreProxy extends TxLogProxy implements ParticipantStore {
    private ParticipantStoreBeanMBean psProxy;	// proxy for the participant store

    public ParticipantStoreProxy(ParticipantStoreBeanMBean rsProxy) {
        super(rsProxy);
		this.psProxy = rsProxy;
	}

    public boolean commit_state(Uid u, String tn) throws ObjectStoreException {
        return psProxy.commit_state(u, tn);
    }

    public InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException {
        return psProxy.read_committed(u, tn).getIOS();
    }

    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException {
        return psProxy.read_uncommitted(u, tn).getIOS();
    }

    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException {
        return psProxy.remove_uncommitted(u, tn);
    }

    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        return psProxy.write_uncommitted(u, tn, new OutputObjectStateWrapper(buff));
    }

    public boolean fullCommitNeeded() {
        return psProxy.fullCommitNeeded();
    }
}
