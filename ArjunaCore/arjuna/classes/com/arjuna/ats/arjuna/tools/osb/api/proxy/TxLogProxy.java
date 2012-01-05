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
import com.arjuna.ats.arjuna.objectstore.TxLog;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.OutputObjectStateWrapper;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.TxLogBeanMBean;

/**
 * Remote proxy to a TxLog Store
 */
public class TxLogProxy implements TxLog {
	private TxLogBeanMBean txLogProxy;

	public TxLogProxy(TxLogBeanMBean txLogProxy) {
		this.txLogProxy = txLogProxy;
    }

    // TxLog methods
    public boolean remove_committed (Uid u, String tn) throws ObjectStoreException {
        return txLogProxy.remove_committed(u, tn);
    }

    public boolean write_committed (Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        return txLogProxy.write_committed(u, tn, new OutputObjectStateWrapper(buff));
    }

    public void sync () throws java.io.SyncFailedException, ObjectStoreException {
        txLogProxy.sync();
    }

    // BaseStore methods
    public String getStoreName () {
        return txLogProxy.getStoreName();
    }

    public void start() {
        txLogProxy.start();
    }

    public void stop() {
        txLogProxy.stop();
    }
}
