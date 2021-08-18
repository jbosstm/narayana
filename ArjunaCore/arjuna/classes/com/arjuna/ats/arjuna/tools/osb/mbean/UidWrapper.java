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
import java.lang.reflect.Constructor;
import java.util.List;

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Base class MBean implementation wrapper for MBeans corresponding to a Uid
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class UidWrapper {
	private static final ThreadLocal<String> recordWrapperTypeName = new ThreadLocal<String>();

	public static void setRecordWrapperTypeName(String name) {
		recordWrapperTypeName.set(name);
    }
	public static String getRecordWrapperTypeName() {
		return recordWrapperTypeName.get();
	}

	private String name;

	private final ObjStoreBrowser browser;
	private final String browserMBeanName;
	private final String beantype;
    private final String className;
	private final String ostype;
	private final Uid uid;

	private long tstamp;
	private OSEntryBean mbean;
	boolean registered = false;
	boolean allowRegistration;

	public UidWrapper(Uid uid) {
		this(null, "", "", null, uid);
	}

	public UidWrapper(ObjStoreBrowser browser, String beantype, String ostype, String className, Uid uid) {
		this(browser, beantype, ostype, className, uid, true);
	}

	public UidWrapper(ObjStoreBrowser browser, String beantype, String ostype, String className, Uid uid, boolean allowRegistration) {
		this.browser = browser;
		this.ostype = ostype;
		this.beantype = beantype;
        this.className = className;
		this.uid = uid;
		this.tstamp = 0L;
		this.browserMBeanName = browser != null ?
				browser.getObjStoreBrowserMBeanName() : arjPropertyManager.getObjectStoreEnvironmentBean().getJmxToolingMBeanName();
		this.name = browserMBeanName + ",itype=" + ostype + ",uid=" + uid.fileStringForm(); // + ",participant=false";
		this.registered = false;
		this.allowRegistration = allowRegistration;
	}

	public OSEntryBean getMBean() {
		return mbean;
	}

    /**
     * Refresh the management view of the whole ObjectStore
     * @throws MBeanException 
     */
	public void probe() throws MBeanException {
		if (browser != null)
			browser.probe();
	}

	public String getType() {
		return ostype;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	void register() {
		if (allowRegistration && mbean != null && !registered) {
			mbean.register();
			registered = true;
		}
	}

	public void unregister() {
		if (registered && mbean != null) {
			try {
				mbean.unregister();
			} catch (Exception e) {

			}
			registered = false;
		}
	}

    /**
     * The timestamp represent the time (in milliseconds) when the bean was registered.
     * It is used for deciding when a bean needs unregistering.
     * @return the timestamp
     */
	public long getTimestamp() {
		return tstamp;
	}

	public void setTimestamp(long tstamp) {
		this.tstamp = tstamp;
	}

	public Uid getUid() {
        return uid;
    }

	public ObjStoreBrowser getBrowser() {
		return browser;
	}

	public String getBrowserMBeanName() {
		return browserMBeanName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UidWrapper that = (UidWrapper) o;

        return !(uid != null ? !uid.equals(that.uid) : that.uid != null);

    }

	@Override
	public int hashCode() {
		return uid != null ? uid.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "UidWrapper{" +
				"ostype='" + ostype + '\'' +
				", uid=" + uid +
				", tstamp=" + tstamp +
				'}';
	}

	public StringBuilder toString(String prefix, StringBuilder sb) {
		return mbean == null ? sb : mbean.toString(prefix, sb);
	}

	public List<UidWrapper> probe(String type) {
		return browser.probe(type);
	}

    /**
     * Construct an MBean to represent this ObjectStore record. The bean type used
     * for construct the MBean is provided in the configuration of the @see ObjStoreBrowser
     * @return the mbean representation
     */
	public OSEntryBean createMBean() {
		try {
			Class<OSEntryBean> cl = (Class<OSEntryBean>) Class.forName(beantype);
			Constructor<OSEntryBean> constructor = cl.getConstructor(UidWrapper.class);
			mbean = constructor.newInstance(this);
		} catch (Throwable e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
			tsLogger.i18NLogger.warn_osb_MBeanCtorFail(e);
			mbean = new OSEntryBean(this);           
        }

		mbean.activate();

		return mbean;
	}

	public void createAndRegisterMBean() {
		if (mbean == null)
		    createMBean();
		register();
	}

	public boolean isAllowRegistration() {
		return allowRegistration;
	}

	public void setAllowRegistration(boolean allowRegistration) {
		this.allowRegistration = allowRegistration;
	}
}
