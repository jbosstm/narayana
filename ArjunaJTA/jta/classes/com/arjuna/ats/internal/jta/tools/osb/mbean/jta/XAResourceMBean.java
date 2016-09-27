/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapperMBean;

/**
 * MBean for XAResourceRecords
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
@MXBeanDescription("Management view of an XAResource participating in a transaction")
public interface XAResourceMBean extends LogRecordWrapperMBean {
	@MXBeanPropertyDescription("The java type that implements this XAResource")
	String getClassName();
	@MXBeanPropertyDescription("JCA product name")
	String getEisProductName();
	@MXBeanPropertyDescription("JNDI Name of the datasource (or the uid if the jndi name is unavailable or if this resource is not a datasource)")
	String getJndiName();
	@MXBeanPropertyDescription("JCA product version")
	String getEisProductVersion();
	@MXBeanPropertyDescription("The number of seconds before the resource can rollback the branch")
	int getTimeout();
	@MXBeanPropertyDescription("The global id part of the XA Transaction Identifier")
    byte[] getGlobalTransactionId();
	@MXBeanPropertyDescription("The branch id part of the XA Transaction Identifier")
	byte[] getBranchQualifier();
	@MXBeanPropertyDescription("The format id part of the XA Transaction Identifier")
	int getFormatId();
	@MXBeanPropertyDescription("The server node id where this record was originally created")
	String getNodeName() ;
	@MXBeanPropertyDescription("An internal representation of the heuristic state of this record")
	int getHeuristicValue();
	@MXBeanPropertyDescription("Attempt to contact the resource to inform it that it can now forget any heuristic decisions it made")
	boolean forget();
}
