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
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.tools.osb.mbean.common.LogRecordListBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.common.LogRecordBean;
import com.arjuna.ats.arjuna.tools.osb.util.ActionWrapper;

import javax.management.ObjectInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

/**
 * @see BasicActionBeanMBean
 */
public class BasicActionBean extends ObjStoreEntryBean implements BasicActionBeanMBean
{
    protected ActionWrapper action_;
    protected Map<String, LogRecordListBean> lists = new HashMap<String, LogRecordListBean>();
    private boolean activated;

    public BasicActionBean(ObjStoreTypeBean parent, Uid uid)
    {
        super(parent, uid);
    }

    public ObjectInstance register()
    {
        if (activated)
            remove();

        if (action_ == null)
            action_ = new BasicActionWrapper(uid);

        try {
            action_.getAction().activate();
        } catch (Exception e) {
            addError(e.getMessage());
        }

        action_.populateLists(lists, this);

        for (LogRecordListBean listBean : lists.values())
            listBean.register();

        activated = true;

        return super.register();
    }

    public void remove()
    {

        if (action_ == null)
            return;

        action_.getAction().deactivate();
        activated = false;

        try {
            action_.remove();
        } catch (ObjectStoreException e) {
            throw new RuntimeException(e);
        }

        action_ = null;

        // record successfuly remove the mbean
        unregister();
    }

    public int getFailedCount()
    {
        return getListSize("Failed");
    }
    public int getHeuristicCount()
    {
        return getListSize("Heuristic");
    }
    public int getPendingCount()
    {
        return getListSize("Pending");
    }
    public int getPreparedCount()
    {
        return getListSize("Prepared");
    }
    public int getReadOnlyCount()
    {
        return getListSize("ReadOnly");
    }

    private int getListSize(String type) {
        LogRecordListBean lb = lists.get(type);

        return (lb == null ? 0 : lb.getSize());
    }

    public String[] getFailedList()
    {
        return toStringArray(lists.get("Failed"));
    }

    public String[] getHeuristicList()
    {
        return toStringArray(lists.get("Heuristic"));
    }

    public String[] getPendingList()
    {
        return toStringArray(lists.get("Pending"));
    }

    public String[] getPreparedList()
    {
        return toStringArray(lists.get("Prepared"));
    }

    public String[] getReadOnlyList()
    {
        return toStringArray(lists.get("Readonly"));
    }

    public static String[] toStringArray(LogRecordListBean listBean)
    {
        Collection<LogRecordBean> beans = listBean.getRegisteredMBeans();

        if (beans != null) {
            String[] res = new String[beans.size()];
            int i = 0;

            for (LogRecordBean bean : beans)
                res[i++] = bean.getObjectName();

            return res;
        }

        return new String[0];
    }

    public static String[] toStringArray(RecordList rl)
    {
        String[] res = new String[rl.size()];
        AbstractRecord rec = rl.peekFront();
        int i = 0;

        while (rec != null)
        {
            res[i++] = rec.getTypeOfObject();
            rec = rl.peekNext(rec);
        }

        return res;
    }

    public boolean unregister()
    {
        for (Iterator<Map.Entry<String, LogRecordListBean>> i = lists.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry<String, LogRecordListBean> entry = i.next();
            if (entry.getValue().unregister())
                i.remove();
        }

        return (lists.size() == 0 && super.unregister());
    }

    public class BasicActionWrapper extends com.arjuna.ats.arjuna.coordinator.BasicAction implements ActionWrapper
    {
        public BasicActionWrapper (Uid objUid)
        {
            super(objUid);
        }

        public com.arjuna.ats.arjuna.coordinator.BasicAction getAction() {
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
