package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.Uid;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Base class MBean implementation wrapper for MBeans corresponding to a Uid
 */
public class UidWrapper {
	private String name;
	private ObjStoreBrowser browser;
	private String beantype;
	private String ostype;
	private Uid uid;
	private long tstamp;
	private OSEntryBean mbean;

	public UidWrapper(Uid uid) {
		this.uid = uid;
		this.name = "";
		this.beantype = "";
		this.ostype = "";
	}

	public OSEntryBean getMBean() {
		return mbean;
	}

	public UidWrapper(ObjStoreBrowser browser, String beantype, String ostype, Uid uid) {
		this.browser = browser;
		this.ostype = ostype;
		this.beantype = beantype;
		this.uid = uid;
		this.tstamp = 0L;
		this.name = "jboss.jta:type=ObjectStore,itype=" + ostype + ",uid=" + uid.fileStringForm(); // + ",participant=false";
	}

    /**
     * Refresh the management view of the whole ObjectStore
     */
	public void probe() {
		browser.probe();
	}

	public String getType() {
		return ostype;
	}

	public String getName() {
		return name;
	}

	void register() {
		mbean.register();
	}

	void unregister() {
		mbean.unregister();
	}

    /**
     * The timestamp represent the time (in milliseconds) when the bean was registered.
     * It is used for deciding when a bean needs unregistering.
     * @return
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
		return mbean.toString(prefix, sb);
	}

	public List<UidWrapper> probe(String type, String beantype) {
		return browser.probe(type, beantype);
	}

    /**
     * Construct an MBean to represent this ObjectStore record. The bean type used
     * for construct the MBean is provided in the configuration of the @see ObjStoreBrowser
     * @return
     */
	public OSEntryBean createMBean() {
		try {
			Class<OSEntryBean> cl = (Class<OSEntryBean>) Class.forName(beantype);
			Constructor<OSEntryBean> constructor = cl.getConstructor(UidWrapper.class);
			mbean = constructor.newInstance(this);
		} catch (Throwable e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
			tsLogger.i18NLogger.info_osb_MBeanCtorFail(e);
			mbean = new OSEntryBean(this);           
        }

		mbean.activate();

		return mbean;
	}
}
