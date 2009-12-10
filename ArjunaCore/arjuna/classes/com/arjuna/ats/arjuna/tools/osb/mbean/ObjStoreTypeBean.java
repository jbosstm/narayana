/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2009
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Constructor;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.common.BasicBean;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import javax.management.ObjectInstance;

/**
 * MBean corresponding to an Object Store type.
 * An Ojbect Store type correspond to the type of record that can be stored in a given store location.
 *
 * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
 *
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_1
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_1] - new type: {0} store name: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_2
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_2] - error unpacking uids for type: {0}, : {1}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_3
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_3] - Error unpacking uids for type: {0} : {1}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_4
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_4] - registering bean: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_5
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_5] - Warning: no viewer for object store type {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_6
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_6] - Error registering bean for class {0} : {1}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_7
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_7] - Warning: error {0} creating viewer for object store type {1}.
 */
public class ObjStoreTypeBean extends BasicBean
{
    /*
     * A map for associating Object Store record type names with Java types that know how to represent
     * them as JMX beans.
     * 
     * TODO move this map to an external file to make it easy to add new handlers
     */
    private static final Map<String, String[]> typeHandlers = new HashMap<String, String[]> () {{
        put("StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction", new String[] {
                "com.arjuna.ats.arjuna.tools.osb.mbean.common.AtomicActionBean",
                "com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean"});
        put("Recovery/TransactionStatusManager", new String[] {
                "com.arjuna.ats.arjuna.tools.osb.mbean.common.TransactionStatusManagerItemBean" });

        // JTS record type handlers
        put("StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple", new String[] {
                "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleBean",
                "com.arjuna.ats.arjuna.tools.osb.mbean.BasicActionBean" });
        put("RecoveryCoordinator", new String[] {
                "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.RecoveryCoordinatorBean",
                "com.arjuna.ats.arjuna.tools.osb.mbean.common.UidBean" });
        put("CosTransactions/XAResourceRecord", new String[] {
                "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.JTSXAResourceRecordBean",
                "com.arjuna.ats.arjuna.tools.osb.mbean.common.UidBean" });
//		put("StateManager/AbstractRecord/XAResourceRecord", "org.jboss.jbosstm.tools.jmx.osb.mbeanimpl.JTAXAResourceRecordBean");
    }};

    private String type;
    private String storeName;
    protected List<BasicBean> registeredBeans = new ArrayList<BasicBean> ();

    public ObjStoreTypeBean()
    {
        super();
    }

    public ObjStoreTypeBean(ObjStoreTypeBean parent, String storeName, String type)
    {
        super(parent, type);
        this.parent = parent;
        this.type = type;
        this.storeName = storeName;

        if (tsLogger.arjLoggerI18N.isDebugEnabled())
            tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_1",
                    new Object[] { type, storeName });
    }

    public ObjectStore getStore()
    {
        return ((ObjStoreTypeBean) parent).getStore();
    }

    public String getObjectName()
    {
        return toObjectName(storeName, type).toString();
    }

    protected StringBuilder toObjectName(String storeName, String type)
    {
        StringBuilder on = new StringBuilder();
        String[] nc = type.split("/");

        on.append(parent.getObjectName()).append(',');
        on.append("name=").append(storeName);

        for (int i = 0; i < nc.length; i++) {
            on.append(',').append('L').append(i + 1).append("=").append(nc[i]);
        }

        return on;
    }

    private void allUids(Collection<Uid> uis)
    {
        InputObjectState uids = new InputObjectState();

        try {
            if (getStore().allObjUids(type, uids)) {
                try {
                    boolean endOfUids = false;

                    while (!endOfUids) {
                        Uid theUid = UidHelper.unpackFrom(uids);

                        if (theUid.equals(Uid.nullUid()))
                            endOfUids = true;
                        else
                            uis.add(new Uid(theUid));
                    }
                } catch (IOException e) {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                        tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_2",
                                new Object[] { type, e });

                }
            }
        } catch (ObjectStoreException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_3",
                        new Object[] { type, e });

        }
    }

    @Override
    public void refresh()
    {
        clearErrors();
        unregisterDependents(true);
        register();
        Iterator<BasicBean> i = registeredBeans.iterator();

        while (i.hasNext()) {
            BasicBean bb = i.next();

            if (!bb.isMarked()) {
                i.remove();
                bb.unregister();
            }
        }
    }

    public ObjectInstance register()
    {
        Collection<Uid> uids = new ArrayList<Uid>();
        ObjectInstance oi;
        BasicBean bean;

        if (tsLogger.arjLoggerI18N.isDebugEnabled())
            tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_4",
                    new Object[] { getObjectName() });

        allUids(uids);
        oi = super.register(); // register this

        if (!uids.isEmpty()) {
//            oi = super.register(); // register this

            for (Uid uid : uids) {
                if ((bean = createBean(uid)) != null)
                    registeredBeans.add(bean);
            }
        }

        return oi;
    }

    public boolean unregister()
    {
        return super.unregister();
    }

    public void unregisterDependents(boolean markOnly)
    {
        for (BasicBean osBean : registeredBeans) {
            if (markOnly)
                osBean.mark();
            else
                osBean.unregister();
        }

        if (!markOnly)
            registeredBeans.clear();
    }

    private BasicBean createBean(Uid uid)
    {
        String[] cnames = typeHandlers.get(type);

        if (cnames == null) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_5",
                        new Object[] { type });

            cnames = new String[] { "com.arjuna.ats.tools.osb.mbean.ObjStoreEntryBean" };
        }

        for (String cname : cnames) {
            try {
                Class<BasicBean> cl = (Class<BasicBean>) Class.forName(cname);
                Constructor<BasicBean> ccon = cl.getConstructor(ObjStoreTypeBean.class, Uid.class);
                BasicBean action = ccon.newInstance(this, uid);

                for (BasicBean registeredBean : registeredBeans) {
                    if (registeredBean == action) {
                        registeredBean.mark();
                        return null;
                    }
                }

                // TODO make sure all the parent beans are registered
                action.register();

                return action;
            } catch (Exception e) {
                if (tsLogger.arjLoggerI18N.isDebugEnabled())
                    tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_7",
                            new Object[] { e.getMessage(), type });                
            }
        }

        if (tsLogger.arjLoggerI18N.isWarnEnabled())
            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean.m_6",
                    new Object[] { type, "No suitable handler" });

        return null;
    }
}
