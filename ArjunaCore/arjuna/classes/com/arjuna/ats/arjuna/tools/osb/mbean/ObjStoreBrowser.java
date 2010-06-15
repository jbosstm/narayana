package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

import com.arjuna.ats.arjuna.common.*;

/**
 * An MBean implementation for walking an ObjectStore and creating/deleting MBeans
 * that represent completing transactions (ie ones on which the user has called commit)
 */
public class ObjStoreBrowser implements ObjStoreBrowserMBean {
	private static final String STORE_MBEAN_NAME = "jboss.jta:type=ObjectStore";
	private static final String OS_BEAN_PROPFILE = "osmbean.properties";

    // define which object store types can be represented by mbeans
	private Properties typeHandlers;

	private Map<String, List<UidWrapper>> allUids;

	public static void main(String[] args) throws Exception {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		String logDir = System.getProperty("objectstore.dir");
		ObjStoreBrowser browser = new ObjStoreBrowser(logDir);

		browser.start();

		do {
			System.out.println("> "); System.out.flush();
			String[] req = br.readLine().split("\\s+");

			if (req.length == 0 || "quit".equals(req[0]))
				break;

			browser.probe();

			if ("dump".equals(req[0])) {
				if (req.length == 1) {
					System.out.println("Uid not found");
				} else {
					UidWrapper w = browser.findUid(req[1]);

					if (w != null)
						System.out.println("Attributes: " + w.toString("", new StringBuilder()));
					else
						System.out.println("Uid not found: " + req[1]);
				}
			} else if ("list".equals(req[0])) {
				System.out.println(browser.dump(new StringBuilder()));
			} //else if ("query".equals(req[0])) {
			  //  browser.queryTest();
		} while (true);
	}

	public static Properties loadProperties(String fname) {
		Properties properties = new Properties();
		URL url = ClassLoader.getSystemResource(fname);
		try {
			if (url != null)
				properties.load(url.openStream());
		} catch (IOException e) {
		}

		return properties;
	}

    /**
     * Initialise the MBean
     */
	public void start()
	{
		JMXServer.getAgent().registerMBean(STORE_MBEAN_NAME, this);
		probe();
	}

    /**
     * Unregister all MBeans representing objects in the ObjectStore
     * represented by this MBean
     */
	public void stop()
	{
		for (List<UidWrapper> uids : allUids.values()) {
			for (Iterator<UidWrapper> i = uids.iterator(); i.hasNext(); ) {
				UidWrapper w = i.next();
				i.remove();
				w.unregister();
			}
		}

		JMXServer.getAgent().unregisterMBean(STORE_MBEAN_NAME);
	}

    /**
     * Define which object store types will registered as MBeans
     * @param types the list of ObjectStore types that can be represented
     * as MBeans
     */
	public void setTypes(Map<String, String> types) {
		for (Map.Entry<String, String> entry : types.entrySet()) {
			if (tsLogger.arjLogger.isDebugEnabled())
				tsLogger.arjLogger.debug("ObjStoreBrowser: adding type handler " + entry.getKey() + "," + entry.getValue());
			typeHandlers.put(entry.getKey(), entry.getValue());
		}
	}

	private void init(String logDir) {
		if (logDir != null)
			arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(logDir);

		if (tsLogger.arjLogger.isDebugEnabled())
			tsLogger.arjLogger.debug("ObjectStoreDir: " + arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir());

		allUids = new HashMap<String, List<UidWrapper>> ();
		typeHandlers = loadProperties(OS_BEAN_PROPFILE);
        
		if (typeHandlers.size() == 0)
			typeHandlers = loadProperties("META-INF/" + OS_BEAN_PROPFILE);
	}

	public ObjStoreBrowser() {
		init(null);
	}

	public ObjStoreBrowser(String logDir) {
		init(logDir);
	}

	public StringBuilder dump(StringBuilder sb) {
		for (Map.Entry<String, List<UidWrapper>> typeEntry : allUids.entrySet()) {
			sb.append(typeEntry.getKey()).append('\n');

			for (UidWrapper uid : typeEntry.getValue())
				uid.toString("\t", sb);
		}

		return sb;
	}

    /**
     * See if the given uid has previously been registered as an MBean
     * @param uid the unique id representing an ObjectStore entry
     * @return the MBean wrapper corresponding to the requested Uid (or null
     * if it hasn't been registered)
     */
	public UidWrapper findUid(Uid uid) {
		return findUid(uid.stringForm());
	}

	public UidWrapper findUid(String uid) {
		for (Map.Entry<String, List<UidWrapper>> typeEntry : allUids.entrySet())
			for (UidWrapper w : typeEntry.getValue())
				if (w.getUid().stringForm().equals(uid))
					return w;

		return null;
	}

    /**
     * See if any new MBeans need to be registered or if any existing MBeans no longer exist
     * as ObjectStore entries.
     */
	public void probe() {
		InputObjectState types = new InputObjectState();

		try {
			if (TxControl.getStore().allTypes(types)) {
				String tname;

				do {
					try {
						tname = types.unpackString();

						if (tname.length() != 0) {
							List<UidWrapper> uids = allUids.get(tname);
							if (uids == null) {
								uids = new ArrayList<UidWrapper> ();
								allUids.put(tname, uids);
							}

							if (typeHandlers.containsKey(tname))
								updateMBeans(uids, System.currentTimeMillis(), true, tname, typeHandlers.getProperty(tname));
						}
					} catch (IOException e1) {
						tname = "";
					}
				} while (tname.length() != 0);
			}
		} catch (ObjectStoreException e2) {
			if (tsLogger.arjLogger.isDebugEnabled())
				tsLogger.arjLogger.debug(e2.toString());
		}
	}

    /**
     * Register new MBeans of the requested type (or unregister ones whose
     * corresponding ObjectStore entry has been removed)
     * @param type the ObjectStore entry type
     * @param beantype the class name of the MBean implementation used to represent
     * the request type
     * @return the list of MBeans representing the requested ObjectStore type
     */
	public List<UidWrapper> probe(String type, String beantype) {
		if (!allUids.containsKey(type))
			return null;

		List<UidWrapper> uids = allUids.get(type);

		updateMBeans(uids, System.currentTimeMillis(), false, type, beantype);

		return uids;
	}

	private void updateMBeans(List<UidWrapper> uids, long tstamp, boolean register, String type, String thandler) {
		ObjectStoreIterator iter = new ObjectStoreIterator(TxControl.getStore(), type);

		while (true) {
			Uid u = iter.iterate();
			if (Uid.nullUid().equals(u))
				break;

			UidWrapper w = new UidWrapper(this, thandler, type, u);
			int i = uids.indexOf(w);

			if (i == -1) {
				w.setTimestamp(tstamp);
				uids.add(w);
				w.createMBean();
				if (register)
					w.register();
			} else {
				uids.get(i).setTimestamp(tstamp);
			}
		}

		for (Iterator<UidWrapper> i = uids.iterator(); i.hasNext(); ) {
			UidWrapper w = i.next();

			if (w.getTimestamp() != tstamp) {
				if (register)
					w.unregister();
				i.remove();
			}
		}
	}
}
