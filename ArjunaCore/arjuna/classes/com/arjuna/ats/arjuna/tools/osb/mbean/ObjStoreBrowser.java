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
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerStatus;

/**
 * An MBean implementation for walking an ObjectStore and creating/deleting MBeans
 * that represent completing transactions (ie ones on which the user has called commit)
 */
public class ObjStoreBrowser implements ObjStoreBrowserMBean {

    private static class OSBType {
        boolean enabled;
        String recordClass; // defines which object store record types will be instrumented
        String beanClass; // the JMX mbean representation of the record type
        String typeName; // the type name {@link com.arjuna.ats.arjuna.coordinator.AbstractRecord#type()}

        private OSBType(boolean enabled, String recordClass, String beanClass, String typeName) {
            this.enabled = enabled;
            this.recordClass = recordClass;
            this.beanClass = beanClass;
            this.typeName = typeName;
        }
    }

    private static final String SUBORDINATE_AA_TYPE =
            "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/SubordinateAtomicAction/JCA";

    private static OSBType[] defaultOsbTypes = {
            new OSBType(
                    true,
                    "com.arjuna.ats.internal.jta.recovery.arjunacore.RecoverConnectableAtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.RecoverConnectableAtomicActionBean",
//                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.CommitMarkableResourceRecordBean",
//                    "com.arjuna.ats.arjuna.AtomicAction",
//                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean",
                    "StateManager/BasicAction/TwoPhaseCoordinator/AtomicActionConnectable"),
            new OSBType(
                    false,
                    "com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.SubordinateActionBean",
                    SUBORDINATE_AA_TYPE),
            new OSBType(
                    true,
                    "com.arjuna.ats.arjuna.AtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean",
                    "StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction"
            ),
            new OSBType(
                    true,
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper",
                    "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean",
                    ""
            )

    };

    private Map<String, OSBType> osbTypeMap = new HashMap<>();

    // A system property for defining extra bean types for instrumenting object store types
    // The format is OSType1=BeanType1,OSType2=BeanType2,etc
    public static final String OBJ_STORE_BROWSER_HANDLERS = "com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowserHandlers";
    private static final String STORE_MBEAN_NAME = "jboss.jta:type=ObjectStore";

    private Map<String, List<UidWrapper>> allUids;
    private boolean exposeAllLogs = false;

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
        try {
            Class cls = Class.forName(osTypeClassName);
            StateManager sm = (StateManager) cls.getConstructor().newInstance();
            String typeName = sm.type();

            if (typeName != null && typeName.startsWith("/"))
                typeName = typeName.substring(1);

            osbTypeMap.put(typeName, new OSBType(true, osTypeClassName, beanTypeClassName, typeName));

            return true;
        } catch (Exception e) {
            if (tsLogger.logger.isDebugEnabled())
                tsLogger.logger.debug("Invalid class type in system property ObjStoreBrowserHandlers: " + osTypeClassName);

            return false;
        }
    }

    private void initTypeHandlers(String handlers) {
        for (String h : handlers.split(",")) {
            String[] handler = h.split("=");

            if (handler.length == 2) {
                setType(handler[0], handler[1]);
            }
        }
    }

    private void init(String logDir) {
        if (logDir != null)
            arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(logDir);

        if (tsLogger.logger.isTraceEnabled())
            tsLogger.logger.trace("ObjectStoreDir: " + arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir());

        setExposeAllRecordsAsMBeans(arjPropertyManager.
            getObjectStoreEnvironmentBean().getExposeAllLogRecordsAsMBeans());

        for (OSBType osbType : defaultOsbTypes)
            osbTypeMap.put(osbType.typeName, osbType);

        allUids = new HashMap<String, List<UidWrapper>> ();

//        initTypeHandlers(defaultStateHandlers);
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

    public void viewSubordinateAtomicActions(boolean enable) {
        OSBType osbType = osbTypeMap.get(SUBORDINATE_AA_TYPE);

        if (osbType == null)
            return;

        osbType.enabled = enable;

        if (!enable) {
            for (List<UidWrapper> uids : allUids.values()) {
                for (Iterator<UidWrapper> i = uids.iterator(); i.hasNext(); ) {
                    UidWrapper w = i.next();
                    if (osbType.recordClass.equals(w.getClassName())) {
                        i.remove();
                        w.unregister();
                    }
                }
            }
        }
    }

    public void setExposeAllRecordsAsMBeans(boolean exposeAllLogs) {
        this.exposeAllLogs = exposeAllLogs;
    }

    private RecoveryManagerStatus trySuspendRM() {
        return RecoveryManager.manager().trySuspend(true);
    }

    private void tryResumeRM(RecoveryManagerStatus previousStatus) {
        if (previousStatus.equals(RecoveryManagerStatus.ENABLED))
            RecoveryManager.manager().resume();
    }
    /**
     * See if any new MBeans need to be registered or if any existing MBeans no longer exist
     * as ObjectStore entries.
     */
    public void probe() {
        updateAllUids();
        Iterator<String> iterator = allUids.keySet().iterator();
        RecoveryManagerStatus rmStatus = trySuspendRM();

        try {
            while (iterator.hasNext()) {
                String tname = iterator.next();
                List<UidWrapper> uids = allUids.get(tname);

                if (uids == null) {
                    uids = new ArrayList<UidWrapper>();
                    allUids.put(tname, uids);
                }

                if (exposeAllLogs || osbTypeMap.containsKey(tname))
                    updateMBeans(uids, System.currentTimeMillis(), true, tname);
            }
        } finally {
            tryResumeRM(rmStatus);
        }

    }

    /**
     * Register new MBeans of the requested type (or unregister ones whose
     * corresponding ObjectStore entry has been removed)
     * @param type the ObjectStore entry type
     * @return the list of MBeans representing the requested ObjectStore type
     */
    public List<UidWrapper> probe(String type) {
		if (!allUids.containsKey(type))
            updateAllUids();

        List<UidWrapper> uids = allUids.get(type);

        if (uids != null && uids.size() > 0) {
            RecoveryManagerStatus rmStatus = trySuspendRM();

            try {
                updateMBeans(uids, System.currentTimeMillis(), false, type);
            } finally {
                tryResumeRM(rmStatus);
            }
        }

        return uids;
    }

    /**
     * See if any new MBeans need to be registered or if any existing MBeans no longer exist
     * as ObjectStore entries.
     */
    private void updateAllUids() {
        InputObjectState types = new InputObjectState();

        try {
            if (StoreManager.getRecoveryStore().allTypes(types)) {
                String tname;

                do {
                    try {
                        tname = types.unpackString();
                    } catch (IOException e1) {
                        tname = "";
                    }

                    if (tname.length() != 0) {
                        List<UidWrapper> uids = allUids.get(tname);

                        if (uids == null) {
                            uids = new ArrayList<UidWrapper>();
                            allUids.put(tname, uids);
                        }
                    }
                } while (tname.length() != 0);
            }
        } catch (ObjectStoreException e2) {
            if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace(e2.toString());
        }
    }

    private void updateMBeans(List<UidWrapper> uids, long tstamp, boolean register, String type) {
        OSBType osbType = osbTypeMap.get(type);

        if (osbType != null && !osbType.enabled)
            return;

        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), type);
        String beanType = osbType == null ? OSEntryBean.class.getName() : osbType.beanClass;
        String stateType = osbType == null ? null : osbType.recordClass;

        while (true) {
            Uid u = iter.iterate();

            if (u == null || Uid.nullUid().equals(u))
                break;

            UidWrapper w = new UidWrapper(this, beanType, type, stateType, u);
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
