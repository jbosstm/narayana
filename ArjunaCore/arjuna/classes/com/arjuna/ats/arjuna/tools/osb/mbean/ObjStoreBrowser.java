package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

/**
 * An MBean implementation for walking an ObjectStore and creating/deleting MBeans
 * that represent completing transactions (ie ones on which the user has called commit)
 */
public class ObjStoreBrowser implements ObjStoreBrowserMBean {
    public static final String OBJ_STORE_BROWSER_HANDLERS = "com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowserHandlers";
    private static final String STORE_MBEAN_NAME = "jboss.jta:type=ObjectStore";

    // defines a (default) map of object store types to the corresponding MBean for instrumentation.
    // The format is OSType1=BeanType1,OSType2=BeanType2,etc
    // Can be over-ridden by setting a system property called com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowserHandlers
    private static final String saaStateType = "com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction";
    private static final String saaBeanType = "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.SubordinateActionBean";
    private static final String defaultStateHandlers =
            "com.arjuna.ats.arjuna.AtomicAction=com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean"
//            + ",com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction=com.arjuna.ats.internal.jta.tools.osb.mbean.jta.SubordinateActionBean"
                    + ",com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper=com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean";

    private Map<String, String> stateTypes = null; // defines which object store types will be instrumented
    private Map<String, String> beanTypes = null;  // defines which bean types are used to represent object store types
    private Map<String, List<UidWrapper>> allUids;

    /**
     * Initialise the MBean
     */
    public void start()
    {
        JMXServer.getAgent().registerMBean(STORE_MBEAN_NAME, this);
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
     * This method is deprecated in favour of @setType
     * The issue with this method is there is no mechanism for determining which class
     * is responsible for a given OS type.
     *
     * Define which object store types will registered as MBeans
     * @param types the list of ObjectStore types that can be represented
     * as MBeans
     */
    @Deprecated
    public void setTypes(Map<String, String> types) {
    }

    /**
     * Tell the browser which beans to use for particular Object Store Action type
     * @param osTypeClassName
     * @param beanTypeClassName
     * @return
     */
    public boolean setType(String osTypeClassName, String beanTypeClassName) {
        String typeName = getOSType(osTypeClassName);

        if (typeName != null) {

            if (typeName.startsWith("/"))
                typeName = typeName.substring(1);

            stateTypes.put(typeName, osTypeClassName);
            beanTypes.put(typeName, beanTypeClassName);

            return true;
        }

        return false;
    }

    private void initTypeHandlers(String handlers) {
        for (String h : handlers.split(",")) {
            String[] handler = h.split("=");

            if (handler.length == 2) {
                String typeName = getOSType(handler[0]);

                if (typeName != null) {

                    if (typeName.startsWith("/"))
                        typeName = typeName.substring(1);

                    stateTypes.put(typeName, handler[0]);
                    beanTypes.put(typeName, handler[1]);
                }
            }
        }
    }

    private void init(String logDir) {
        if (logDir != null)
            arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(logDir);

        if (tsLogger.logger.isTraceEnabled())
            tsLogger.logger.trace("ObjectStoreDir: " + arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir());

        allUids = new HashMap<String, List<UidWrapper>> ();
        stateTypes = new HashMap<String, String>();
        beanTypes = new HashMap<String, String>();

        initTypeHandlers(defaultStateHandlers);
        initTypeHandlers(System.getProperty(OBJ_STORE_BROWSER_HANDLERS, ""));
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
    ;
    private String getOSType(String classType) {
        try {
            Class cls = Class.forName(classType);
            StateManager sm = (StateManager) cls.getConstructor().newInstance();

            return sm.type();
        } catch (Exception e) {
            if (tsLogger.logger.isDebugEnabled())
                tsLogger.logger.debug("Invalid class type in system property ObjStoreBrowserHandlers: " + classType);
        }

        return null;
    }
    /**
     * See if any new MBeans need to be registered or if any existing MBeans no longer exist
     * as ObjectStore entries.
     */
    public void probe() {
        InputObjectState types = new InputObjectState();

        try {
            if (StoreManager.getRecoveryStore().allTypes(types)) {
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

                            if (beanTypes.containsKey(tname))
                                updateMBeans(uids, System.currentTimeMillis(), true, tname);
                        }
                    } catch (IOException e1) {
                        tname = "";
                    }
                } while (tname.length() != 0);
            }
        } catch (ObjectStoreException e2) {
            if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace(e2.toString());
        }
    }

    public void viewSubordinateAtomicActions(boolean enable) {
        if (enable) {
            setType(saaStateType, saaBeanType);
        } else {
            String typeName = getOSType(saaStateType);

            if (typeName != null) {

                if (typeName.startsWith("/"))
                    typeName = typeName.substring(1);

                stateTypes.remove(typeName);
                beanTypes.remove(typeName);

                for (List<UidWrapper> uids : allUids.values()) {
                    for (Iterator<UidWrapper> i = uids.iterator(); i.hasNext(); ) {
                        UidWrapper w = i.next();
                        if (saaStateType.equals(w.getClassName())) {
                            i.remove();
                            w.unregister();
                        }
                    }
                }
            }
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

        updateMBeans(uids, System.currentTimeMillis(), false, type);

        return uids;
    }

    private void updateMBeans(List<UidWrapper> uids, long tstamp, boolean register, String type) {
        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), type);

        while (true) {
            Uid u = iter.iterate();
            if (u == null || Uid.nullUid().equals(u))
                break;


            UidWrapper w = new UidWrapper(this, beanTypes.get(type), type, stateTypes.get(type), u);
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
