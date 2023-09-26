/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.resources.arjunacore;

import java.io.IOException;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * Callback interface to allow customisable population of XAResourceRecord metadata.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2011-07
 */
public interface XAResourceRecordWrappingPlugin
{
    public void transcribeWrapperData(XAResourceRecord record);

    public Integer getEISName(XAResource xaResource) throws IOException, ObjectStoreException;

	public String getEISName(Integer eisName);
}