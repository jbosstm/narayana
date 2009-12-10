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

import javax.management.ObjectInstance;
import javax.management.OperationsException;
import javax.management.MBeanException;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;

/**
 * MBean representation of an Object Store.
 * An Object Store contains a type hierarchy for storing different kinds of record.
 * The hierarchy is reflected in the set of keys of the corresponding MBeans, ie a containing type will
 * have a set of keys that are a strict superset of each of the types further up the hierarchy.
 *
 * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
 *
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_1
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_1] - new store for location: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_2
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_2] - Error creating object store root: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_3
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_3] - Error creating object store root: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_4
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_4] - parsing store type: {0}
 * @message com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_5
 *		  [com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_5] - Browsing store {0} location: {1}
 *
 */
public class ObjStoreBean extends ObjStoreTypeBean
{
    private ObjectStore store;
    private int id;

    private static ObjStoreBean localStore;
    
    public static ObjStoreTypeBean getObjectStoreBrowserBean()
            throws OperationsException, MBeanException, InterruptedException, IOException
    {
        if (localStore == null)
            localStore = (ObjStoreBean) getObjectStoreBrowserBean(0, arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir());

        return localStore;
    }

    public static ObjStoreTypeBean getObjectStoreBrowserBean(int id, String storeDir)
            throws OperationsException, MBeanException, InterruptedException, IOException
    {
        ObjStoreBean sbean = new ObjStoreBean(id, storeDir);

        sbean.register();

        return sbean;
    }

    public ObjStoreBean()
    {
        super();
    }

    private List<String> getStoreNames()
    {
        File osf = new File(store.storeDir());
        List<String> storeNames = new ArrayList<String>();

        if (osf.exists())
            storeNames = Arrays.asList(osf.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            }));

        return storeNames;
    }

    // TODO dont restrict the mbean to filesystem based stores
    public ObjStoreBean(int id, String storeLocation)
    {
        super(null, "/", "root");
        this.id = id;

        if (tsLogger.arjLoggerI18N.isDebugEnabled())
            tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_1",
                    new Object[] { storeLocation });

        store = getObjectStore(storeLocation, null);
    }

    public String getObjectName()
    {
        // return a unique name for this object store
        return "jboss.jta:type=ObjectStore,id=" + id;
    }

    public ObjectStore getStore()
    {
        return store;
    }

    public ObjectInstance register()
    {
        if (arjPropertyManager.getObjectStoreEnvironmentBean().isJmxEnabled()) {
            Collection<ObjStoreTypeBean> beans = new ArrayList<ObjStoreTypeBean> ();
            ObjectInstance oi = JMXServer.getAgent().registerMBean(this);

            if (tsLogger.arjLoggerI18N.isDebugEnabled())
                tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_4",
                        new Object[] { arjPropertyManager.getCoordinatorEnvironmentBean().getActionStore() });

            for (String storeName : getStoreNames()) {
                allTypes(this, storeName, beans);

                if (tsLogger.arjLoggerI18N.isDebugEnabled())
                    tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_5",
                            new Object[] { storeName, store.storeDir() });

                for(ObjStoreTypeBean bean : beans)
                    bean.register();

                registeredBeans.addAll(beans);

                beans.clear();
            }

            super.register();

            return oi;
        }

        return null;
    }

    public boolean unregister()
    {
        return super.unregister();
    }

    public ObjectStore getObjectStore(String storeDir, String storeName)
    {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(storeDir);
        String storeImple = arjPropertyManager.getCoordinatorEnvironmentBean().getActionStore();
        arjPropertyManager.getObjectStoreEnvironmentBean().setLocalOSRoot(storeName);

        try {
            Class osImple = Class.forName(storeImple);

            return (ObjectStore) osImple.newInstance();
        } catch (Exception e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_2",
                        new Object[] { e });
        }

        return null;
    }

    public void allTypes(ObjStoreTypeBean parent, String storeName, Collection<ObjStoreTypeBean> names)
    {
        InputObjectState types = new InputObjectState();

        try {
            if (getObjectStore(store.storeDir(), storeName).allTypes(types)) {
                while (true) {
                    try {
                        String theName = types.unpackString();

                        if (theName.length() == 0)
                            break;
                        else
                            names.add(new ObjStoreTypeBean(parent, storeName, theName));
                    } catch (IOException e) {
                        break; // end of list
                    }
                }
            }
        } catch (ObjectStoreException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBean.m_3",
                        new Object[] { e });
        }
    }

    public static void main(String[] args) throws OperationsException, MBeanException, InterruptedException, IOException {
        String[] storeLocations = {
                "/home/mmusgrov/source/as/trunk/build/output/jboss-6.0.0-SNAPSHOT/server/all/data/tx-object-store",
//                "../../logs/os.2",
        };
        ObjStoreBean[] browsers = new ObjStoreBean[storeLocations.length];

        for (int i = 0; i < storeLocations.length; i++) {
            browsers[i] = new ObjStoreBean(i, storeLocations[i]);
            browsers[i].register();
        }

        Thread.sleep(24000000);
    }
}
