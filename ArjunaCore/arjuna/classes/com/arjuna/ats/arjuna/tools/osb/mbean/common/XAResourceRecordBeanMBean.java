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

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.common.UidBeanMBean;

/**
 * MBean representation of an XA ResourceManager
 */
@MXBeanDescription("Log Record for distributed XA Resources")
public interface XAResourceRecordBeanMBean extends UidBeanMBean
{
    @MXBeanPropertyDescription("The java type that implements this XAResource")
    String getClassName();
    @MXBeanPropertyDescription("JNDI name of the JCA resource")
    String getEisProductName();
    @MXBeanPropertyDescription("JCA product version")
    String getEisProductVersion();
    @MXBeanPropertyDescription("XA xid for the transaction branch")
    String getXid();
    @MXBeanPropertyDescription("The number of seconds before the resource can rollback the branch")
    int getTimeout();
}
