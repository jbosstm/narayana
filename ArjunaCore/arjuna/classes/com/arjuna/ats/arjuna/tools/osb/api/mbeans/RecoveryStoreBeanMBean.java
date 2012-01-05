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

/**
 * JMX interface to the JBossTS recovery store
 * OutputObjectState and InputObjectState are wrapped since they are not convertible to
 * open MBean types.
 *
 * @see com.arjuna.ats.arjuna.tools.osb.api.proxy.RecoveryStoreProxy
 * for the actual remote RecoveryStore proxy
 *
 * @see com.arjuna.ats.arjuna.objectstore.RecoveryStore for the interface it implements
 */
public interface RecoveryStoreBeanMBean extends TxLogBeanMBean
{
    public ObjectStateWrapper allObjUids(String type, int match) throws ObjectStoreException;
    public ObjectStateWrapper allObjUids(String type) throws ObjectStoreException;

    public ObjectStateWrapper allTypes() throws ObjectStoreException;
    public int currentState (Uid u, String tn) throws ObjectStoreException;
    public boolean hide_state (Uid u, String tn) throws ObjectStoreException;
    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException;
    public ObjectStateWrapper read_committed (Uid u, String tn) throws ObjectStoreException;
    public boolean isType (Uid u, String tn, int st) throws ObjectStoreException;
}
