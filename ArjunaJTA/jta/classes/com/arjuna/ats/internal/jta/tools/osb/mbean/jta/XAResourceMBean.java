/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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