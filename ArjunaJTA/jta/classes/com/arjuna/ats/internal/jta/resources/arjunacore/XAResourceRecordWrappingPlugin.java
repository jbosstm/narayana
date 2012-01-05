/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
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
