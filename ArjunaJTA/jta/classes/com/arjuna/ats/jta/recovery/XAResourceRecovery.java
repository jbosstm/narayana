/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.recovery;

import java.sql.SQLException;

import javax.transaction.xa.XAResource;

/**
 * To perform recovery on arbitrary XAResources we may need
 * to obtain new instances. Users can provide implementations
 * of this interface which we will use at recovery time to re-create
 * XAResources and from them perform recovery.
 *
 * @since JTS 3.3.
 */

public interface XAResourceRecovery
{

    /**
     * Get a resource to use for recovery purposes.
     *
     * @return a new XAResource.
     */

    public XAResource getXAResource () throws SQLException;

    /**
     * Initialise with all properties required to create the resource(s).
     *
     * @param p An arbitrary string from which initialization data
     * is obtained.
     *
     * @return <code>true</code> if initialization happened successfully,
     * <code>false</code> otherwise.
     */

    public boolean initialise (String p) throws SQLException;

    /**
     * Iterate through all of the resources this instance provides
     * access to.
     *
     * @return <code>true</code> if this instance can provide more
     * resources, <code>false</code> otherwise.
     */

    public boolean hasMoreResources ();

}