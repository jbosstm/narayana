package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            "StateManager"+File.separator+"BasicAction"+File.separator+"TwoPhaseCoordinator"+File.separator+"AtomicAction"+File.separator+"SubordinateAtomicAction"+File.separator+"JCA";

    private static OSBType[] defaultOsbTypes = {
            new OSBType(
                    true,
                    "com.arjuna.ats.internal.jta.recovery.arjunacore.RecoverConnectableAtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.RecoverConnectableAtomicActionBean",
//                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.CommitMarkableResourceRecordBean",
//                    "com.arjuna.ats.arjuna.AtomicAction",
//                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean",
                    "StateManager"+File.separator+"BasicAction"+File.separator+"TwoPhaseCoordinator"+File.separator+"AtomicActionConnectable"),
            new OSBType(
                    false,
                    "com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.SubordinateActionBean",
                    SUBORDINATE_AA_TYPE),
            new OSBType(
                    true,
                    "com.arjuna.ats.arjuna.AtomicAction",
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jta.JTAActionBean",
                    "StateManager"+File.separator+"BasicAction"+File.separator+"TwoPhaseCoordinator"+File.separator+"AtomicAction"
            ),
            new OSBType(
                    true,
                    "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper",
                    "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean",
                    "StateManager"+File.separator+"BasicAction"+File.separator+"TwoPhaseCoordinator"+File.separator+"ArjunaTransactionImple"
            )

    };

    private Map<String, OSBType> osbTypeMap = new HashMap<String, OSBType>();

    // A system property for defining extra bean types for instrumenting object store types
    // The format is OSType1=BeanType1,OSType2=BeanType2,etc
    public static final String OBJ_STORE_BROWSER_HANDLERS = "com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowserHandlers";
    private static final String STORE_MBEAN_NAME = "jboss.jta:type=ObjectStore";

    private Map<String, List<UidWrapper>> registeredMBeans = new HashMap<String, List<UidWrapper>> ();;
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
        unregisterMBeans();

        JMXServer.getAgent().unregisterMBean(STORE_MBEAN_NAME);
    }

    private void unregisterMBeans(List<UidWrapper> beans) {
        for (UidWrapper w : beans)
            w.unregister();

        beans.clear();
    }

    private void unregisterMBeans() {
        for (List<UidWrapper> uids : registeredMBeans.values())
            unregisterMBeans(uids);

        registeredMBeans.clear();
    }

    private void registerMBeans() {
        for (List<UidWrapper> uids : registeredMBeans.values()) {
            for (UidWrapper w : uids)
                w.createAndRegisterMBean();
        }
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

            typeName = typeName.replaceAll("/", File.separator);
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

        initTypeHandlers(System.getProperty(OBJ_STORE_BROWSER_HANDLERS, ""));
    }

    public ObjStoreBrowser() {
        init(null);
    }

    public ObjStoreBrowser(String logDir) {
        init(logDir);
    }

    /**
     * Dump info about all registered MBeans
     * @param sb a buffer to contain the result
     * @return the passed in buffer
     */
    public StringBuilder dump(StringBuilder sb) {
        for (Map.Entry<String, List<UidWrapper>> typeEntry : registeredMBeans.entrySet()) {
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
        for (Map.Entry<String, List<UidWrapper>> typeEntry : registeredMBeans.entrySet())
            for (UidWrapper w : typeEntry.getValue())
                if (w.getUid().equals(uid))
                    return w;

        return null;
    }

    /**
     * Find the registered beand corresponding to a uid.
     * @deprecated use {@link #findUid(com.arjuna.ats.arjuna.common.Uid)} ()} instead.
     * @param uid the uid
     * @return the registered bean or null if the Uid is not registered
     */
    @Deprecated
    public UidWrapper findUid(String uid) {
        for (Map.Entry<String, List<UidWrapper>> typeEntry : registeredMBeans.entrySet())
            for (UidWrapper w : typeEntry.getValue())
                if (w.getUid().stringForm().equals(uid))
                    return w;

        return null;
    }

    private boolean isRegistered(String type, Uid uid) {
        List<UidWrapper> beans = registeredMBeans.get(type);

        if (beans != null)
            for (UidWrapper w : beans)
                if (uid.equals(w.getUid()))
                    return true;

        return false;
    }

    public void viewSubordinateAtomicActions(boolean enable) {
        OSBType osbType = osbTypeMap.get(SUBORDINATE_AA_TYPE);

        if (osbType == null)
            return;

        osbType.enabled = enable;

        if (!enable) {
            for (List<UidWrapper> uids : registeredMBeans.values()) {
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

    /**
     * Update registered MBeans based on the current set of Uids.
     * @param allCurrUids any registered MBeans not in this collection will be deregistered
     */
    private void unregisterRemovedUids(Map<String, Collection<Uid>> allCurrUids) {

        for (Map.Entry<String, List<UidWrapper>> e : registeredMBeans.entrySet()) {
            String type = e.getKey();
            List<UidWrapper> registeredBeansOfType = e.getValue();
            Collection<Uid> currUidsOfType = allCurrUids.get(type);

            if (currUidsOfType != null) {
                Iterator<UidWrapper> iterator = registeredBeansOfType.iterator();

                while (iterator.hasNext()) {
                    UidWrapper w = iterator.next();

                    if (!currUidsOfType.contains(w.getUid())) {
                        w.unregister();
                        iterator.remove();
                    }
                }
            } else {
                unregisterMBeans(registeredBeansOfType);
            }
        }
    }

    /**
     * See if any new MBeans need to be registered or if any existing MBeans no longer exist
     * as ObjectStore entries.
     */
    public synchronized void probe() {
        Map<String, Collection<Uid>> currUidsForType = new HashMap<String, Collection<Uid>>();

        for (String type : getTypes())
            currUidsForType.put(type, getUids(type));

        // if there are any beans in registeredMBeans that don't appear in new list and unregister them
        unregisterRemovedUids(currUidsForType); //unregisterMBeans();

        for (Map.Entry<String, Collection<Uid>> e : currUidsForType.entrySet()) {
            String type = e.getKey();

            List<UidWrapper> beans = registeredMBeans.get(type);

            if (beans == null) {
                beans = new ArrayList<UidWrapper>();
                registeredMBeans.put(type, beans);
            }

            for (Uid uid : e.getValue()) {
                if (!isRegistered(type, uid)) {
                    UidWrapper w = registerBean(uid, type, false); // can return null if type isn't instrumented

                    if (w != null)
                        beans.add(w);
                }
            }
        }

        /*
         * now create the actual MBeans - we create all the UidWrappers before registering because
         * the process of creating a bean can call back into the browser to probe for a particular type
         * (see for example com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean
         */
        registerMBeans();
    }

    /**
     * Register new MBeans of the requested type (or unregister ones whose
     * corresponding ObjectStore entry has been removed)
     * @param type the ObjectStore entry type
     * @return the list of MBeans representing the requested ObjectStore type
     */
    public List<UidWrapper> probe(String type) {
		return registeredMBeans.get(type);
    }

    private UidWrapper registerBean(Uid uid, String type, boolean createMbean) {
        OSBType osbType = osbTypeMap.get(type);

        if (osbType == null && !exposeAllLogs)
            return null;

        if (osbType != null && !osbType.enabled)
            return null;

        String beanType = osbType == null ? OSEntryBean.class.getName() : osbType.beanClass;
        String stateType = osbType == null ? null : osbType.recordClass;
        UidWrapper w = new UidWrapper(this, beanType, type, stateType, uid);

        if (createMbean)
            w.createAndRegisterMBean();

        return w;
    }

    private Collection<Uid> getUids(String type) {
        Collection<Uid> uids = new ArrayList<Uid>();
        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), type);

        while (true) {
            Uid u = iter.iterate();

            if (u == null || Uid.nullUid().equals(u))
                break;

            uids.add(u);
        }

        return uids;
    }

    private Collection<String> getTypes() {
        Collection<String> allTypes = new ArrayList<String>();
        InputObjectState types = new InputObjectState();

        try {
            if (StoreManager.getRecoveryStore().allTypes(types)) {

                while (true) {
                    try {
                        String typeName = types.unpackString();
                        if (typeName.length() == 0)
                            break;
                        allTypes.add(typeName);
                    } catch (IOException e1) {
                        break;
                    }
                }
            }
        } catch (ObjectStoreException e) {
            if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace(e.toString());        }

        return allTypes;
    }
}
