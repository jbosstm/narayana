/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.common.internal.util.ClassloadingUtility;

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * Information provided to {@link ObjStoreBrowser#registerHandler} for instrumenting record types.
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class OSBTypeHandler {
    private boolean enabled;
    private boolean allowRegistration;

    private String recordClass; // defines which object store record types will be instrumented
    private String beanClass; // the JMX mbean representation of the record type
    private String typeName; // the type name {@link com.arjuna.ats.arjuna.coordinator.AbstractRecord#type()}
    HeaderStateReader headerStateReader;
    private ClassLoader classLoaderToUse; // classLoader to load external bean types

    public OSBTypeHandler(boolean enabled, String recordClass, String beanClass, String typeName, String headerStateReaderClassName) {
        this(enabled, true, recordClass, beanClass, typeName, headerStateReaderClassName);
    }

    public OSBTypeHandler(boolean enabled, boolean allowRegistration, String recordClass, String beanClass, String typeName, String headerStateReaderClassName) {
        this.enabled = enabled;
        this.allowRegistration = allowRegistration;
        this.recordClass = recordClass;
        this.beanClass = beanClass;
        this.typeName = typeName;
        this.headerStateReader = headerStateReaderClassName == null ? new HeaderStateReader() : createHeader(headerStateReaderClassName);
        this.classLoaderToUse = this.getClass().getClassLoader();
    }

    public OSBTypeHandler(boolean enabled, boolean allowRegistration, String recordClass, String beanClass, String typeName, String headerStateReaderClassName, ClassLoader classLoaderToUse) {
        this.enabled = enabled;
        this.allowRegistration = allowRegistration;
        this.recordClass = recordClass;
        this.beanClass = beanClass;
        this.typeName = typeName;
        this.headerStateReader = headerStateReaderClassName == null ? new HeaderStateReader() : createHeader(headerStateReaderClassName);
        this.classLoaderToUse = classLoaderToUse;
    }

    private static HeaderStateReader createHeader(String headerStateReaderClassName) {
		try {
            Class<HeaderStateReader> cl = (Class<HeaderStateReader>) Class.forName(headerStateReaderClassName);
            Constructor<HeaderStateReader> constructor = cl.getConstructor();
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            tsLogger.logger.debugf("OSB: Header reader for class %s not found", headerStateReaderClassName);
		} catch (Throwable e) { // NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
			tsLogger.i18NLogger.info_osb_HeaderStateCtorFail(e);
        }

        return new HeaderStateReader();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRecordClass() {
        return recordClass;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public HeaderStateReader getHeaderStateReader() {
        return headerStateReader;
    }

    // This method is intended for the ObjStoreBrowser only
    // the classLoaderToUse is needed here to load an external bean
    UidWrapper createMBean(ObjStoreBrowser objStoreBrowser, String beantype, String type, String stateType, Uid uid,
            boolean registerBean) {
        UidWrapper w = new UidWrapper(objStoreBrowser, beantype, type, stateType, uid, registerBean);

        OSEntryBean mbean = null;

        try {
            Class<OSEntryBean> cl = (Class<OSEntryBean>) classLoaderToUse.loadClass(beantype);
            Constructor<OSEntryBean> constructor = cl.getConstructor(UidWrapper.class);
            mbean = constructor.newInstance(w);
        } catch (Throwable e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
            tsLogger.i18NLogger.warn_osb_MBeanCtorFail(e);
            mbean = new OSEntryBean(w);
        }

        mbean.activate();

        w.setMBean(mbean);

        return w;
    }
}
