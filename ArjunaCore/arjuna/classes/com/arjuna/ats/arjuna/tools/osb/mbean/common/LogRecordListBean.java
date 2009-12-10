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
package com.arjuna.ats.arjuna.tools.osb.mbean.common;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean;

import javax.management.ObjectInstance;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 * MBean implementation of ObjectStore records that contain other records.
 *
 * @see com.arjuna.ats.arjuna.tools.osb.mbean.common.LogRecordListBeanMBean
 */
public class LogRecordListBean extends BasicBean implements LogRecordListBeanMBean
{
    private Collection<LogRecordBean> registeredBeans = new ArrayList<LogRecordBean>();
    private RecordList records;
    private String name;

    public LogRecordListBean(BasicActionBean parent, RecordList records, String name)
    {
        super(parent, parent.getType());

        this.records = records;
        this.name = name;
    }

    public int getSize()
    {
		return (records == null ? 0 : records.size());
	}

	public String getObjectName()
	{
		return parent.getObjectName() + "T1=" + name;
	}

    public BasicActionBean getParent()
    {
        return (BasicActionBean) parent;
    }

    public String getName()
    {
		return name;
	}

    public Collection<LogRecordBean> getRegisteredMBeans()
    {
		return registeredBeans;
	}

    public ObjectInstance register()
    {
        ObjectInstance oi = null;

        if (getSize() != 0) {
            super.register();
            
            for (AbstractRecord rec = records.peekFront(); rec != null; rec = records.peekNext(rec)) {
                LogRecordBean bean = new LogRecordBean(this, (BasicActionBean) parent, rec);

                if (bean.register() != null)
					registeredBeans.add(bean);
			}
		}

        return oi;
    }

    public void unregisterDependents(boolean markOnly)
    {
        for (LogRecordBean mbean : registeredBeans) {
            if (markOnly)
                mbean.marked = true;
            else
                mbean.unregister();
        }

        if (!markOnly)
            registeredBeans.clear();
    }

    public boolean unregister()
    {
		for (Iterator<LogRecordBean> i = registeredBeans.iterator(); i.hasNext(); ) {
			if (i.next().unregister())
				i.remove();
		}

		return (getSize() == 0 && super.unregister());
	}  
}
