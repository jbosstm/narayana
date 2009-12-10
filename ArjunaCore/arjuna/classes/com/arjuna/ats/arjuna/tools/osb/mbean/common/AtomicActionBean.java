/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009
 * @author JBoss Inc.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean.common;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean;
import com.arjuna.ats.arjuna.tools.osb.util.ActionWrapper;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import javax.management.ObjectInstance;
import java.util.Map;

/**
 * MBean representation of an AtomicAction
 * The AtomicActionWrapper inner class exposes the itentions lists of
 * the actual atomic action
 *
 * @see com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean
 */
public class AtomicActionBean extends BasicActionBean
{
    public AtomicActionBean(ObjStoreTypeBean parent, Uid uid) {
        super(parent, uid);
    }

    public ObjectInstance register()
    {
        if (action_ == null)
            action_ = new AtomicActionWrapper(uid);

        return super.register();
    }

    public class AtomicActionWrapper extends AtomicAction implements ActionWrapper
    {
        public AtomicActionWrapper(Uid objUid)
        {
            super(objUid);
        }

        public com.arjuna.ats.arjuna.coordinator.BasicAction getAction()
        {
            return this;
        }

        public void populateLists(Map<String, LogRecordListBean> lists, BasicActionBean bean)
        {
            lists.put("Heuristic", new LogRecordListBean(bean, heuristicList, "Heuristic List"));
            lists.put("Failed", new LogRecordListBean(bean, failedList, "Failed List"));
            lists.put("Readonly", new LogRecordListBean(bean, readonlyList, "Readonly List"));
            lists.put("Pending", new LogRecordListBean(bean, pendingList, "Pending List"));
            lists.put("Prepared", new LogRecordListBean(bean, preparedList, "Prepared List"));
        }

        public void remove() throws ObjectStoreException
        {
            if (!getStore().remove_committed(getSavingUid(), type()))
                throw new ObjectStoreException();
        }
    }
}

