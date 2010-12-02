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
import com.arjuna.ats.arjuna.objectstore.TxLog;

import javax.management.ObjectName;

/**
 * abstract implementation of the TxLog MBean
 */
public abstract class TxLogBean extends BaseStoreBean implements TxLogBeanMBean {
	private TxLog store;

	public TxLogBean(TxLog store) {
        super(store);
		this.store = store;
    }

    protected TxLog getStore() {
        return store;
    }

    @Override
    protected abstract ObjectName getMBeanName();

    public void sync () throws java.io.SyncFailedException, ObjectStoreException {
		store.sync ();
	}

	public boolean write_committed (Uid u, String tn, OutputObjectStateWrapper buff) throws ObjectStoreException {
		return store.write_committed (u, tn, buff.getOOS());
	}
    
	public boolean remove_committed (Uid u, String tn) throws ObjectStoreException {
		return store.remove_committed(u, tn);
	}

}
