/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.io.IOException;

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

/**
 * Superclass for MBean implementations representing ObjectStore entries
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class OSEntryBean implements OSEntryBeanMBean {	
	protected UidWrapper _uidWrapper;

	public OSEntryBean() {
		this._uidWrapper = new UidWrapper(Uid.nullUid());
	}

	public OSEntryBean(UidWrapper w) {
		if (w == null)
			w = new UidWrapper(Uid.nullUid());

		this._uidWrapper = w;
	}

	public void register(String name) {
		if (tsLogger.logger.isTraceEnabled())
			tsLogger.logger.trace("Registering: " + name);
		JMXServer.getAgent().registerMBean(name, this);
	}

	public void unregister(String name) {
		if (tsLogger.logger.isTraceEnabled())
			tsLogger.logger.trace("Unregistering: " + name);

		JMXServer.getAgent().unregisterMBean(name);
	}

	public void register() {
		register(getName());
	}

	public void unregister() {
		unregister(getName());
	}

	public String getName() {
		return _uidWrapper.getName();
	}

	public String getType() {
		return _uidWrapper.getType();
	}

	public String type() {
		return getType();
	}

	public boolean activate() {
		return false;
	}

	public Uid getUid(AbstractRecord rec) {
		return rec.get_uid();
	}

	public String getId() {
		return _uidWrapper.getUid().stringForm();
	}

	public Uid getUid() {
		return _uidWrapper.getUid();
	}

	public StringBuilder toString(String prefix, StringBuilder sb) {
		return sb.append(prefix).append('\t').append(getId()).append('\n');
	}

	/**
	 * Remove this record from the ObjectStore
	 * @return a textual indication of whether the remove operation succeeded
	 * @throws MBeanException
	 */
	public String remove() throws MBeanException {
		return remove(true);
	}

	public String remove(boolean reprobe) throws MBeanException {
		if (doRemove()) {
			if (reprobe)
				_uidWrapper.probe();

			return REMOVE_OK_1;
		}

		return REMOVE_NOK_1;
	}

	public boolean doRemove() {
		try {
			if (StoreManager.getRecoveryStore().remove_committed(getUid(), _uidWrapper.getType()))
				return true;

			if (tsLogger.logger.isDebugEnabled())
				tsLogger.logger.debugf("%s %s", REMOVE_NOK_1, getUid().toString());

			return false;
		} catch (ObjectStoreException e) {
			if (tsLogger.logger.isDebugEnabled())
				tsLogger.logger.debugf("%s %s - %s", REMOVE_NOK_1, getUid().toString(), e.getMessage());

			return false;
		}
	}

	public static final String REMOVE_OK_1 = "Record successfully removed";
	public static final String REMOVE_NOK_1 = "Remove committed failed for uid ";
}
