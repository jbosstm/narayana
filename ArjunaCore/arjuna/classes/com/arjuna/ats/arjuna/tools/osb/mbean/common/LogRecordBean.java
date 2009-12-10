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

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean;

/**
 * MBean implementation of records that extend StateManager
 *
 * @see com.arjuna.ats.arjuna.tools.osb.mbean.common.StateBeanMBean
 *
 */
public class LogRecordBean extends StateBean // implements LogRecordMXBean
{
    private AbstractRecord record;
    BasicActionBean owner;

    public LogRecordBean(BasicBean parent, BasicActionBean owner, AbstractRecord record)
    {
        super(parent, record.type(), owner.getStore(), record.order());
        this.record = record;
        this.owner = owner;
    }

    public String getRecordType()
    {
        Class recordClass = RecordType.typeToClass(record.typeIs());
        
        return recordClass.getCanonicalName();
    }

    // MXBean method overrides
    // log records inherit their header from Object Store entry
    @Override public String getCreationTime()
    { 
        return owner.getCreationTime();
    }
    @Override public long getAgeInSeconds()
    {
        return owner.getAgeInSeconds();
    }
}
