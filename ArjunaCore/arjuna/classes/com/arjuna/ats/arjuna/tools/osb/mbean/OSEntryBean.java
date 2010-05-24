package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

/**
 * Superclass for MBean implementations representing ObjectStore entries
 */
public class OSEntryBean implements OSEntryBeanMBean {	
	protected UidWrapper w;

	public OSEntryBean() {
		this.w = new UidWrapper(Uid.nullUid());
	}

	public OSEntryBean(UidWrapper w) {
		this.w = w;
	}

	public void register(String name) {
		if (tsLogger.arjLoggerI18N.isDebugEnabled())
			tsLogger.arjLoggerI18N.debug("Registering: " + name);
		JMXServer.getAgent().registerMBean(name, this);
	}

	public void unregister(String name) {
		if (tsLogger.arjLoggerI18N.isDebugEnabled())
			tsLogger.arjLoggerI18N.debug("Unregistering: " + name);

		JMXServer.getAgent().unregisterMBean(name);
	}

	public void register() {
		register(getName());
	}

	public void unregister() {
		unregister(getName());
	}

	public String getName() {
		return w.getName();
	}

	public String getType() {
		return w.getType();
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
		return w.getUid().stringForm();
	}

	public Uid getUid() {
		return w.getUid();
	}

	public StringBuilder toString(String prefix, StringBuilder sb) {
		return sb.append(prefix).append('\t').append(getId()).append('\n');
	}
}
