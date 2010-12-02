/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.objectstore.BaseStore;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.StoreManagerProxy;

import javax.management.*;

/**
 * abstract MBean implementation of a BaseStore MBean
 */
public abstract class BaseStoreBean extends NotificationBroadcasterSupport implements BaseStoreMBean {
	private BaseStore store;

	public BaseStoreBean(BaseStore store) {
		this.store = store;
	}

    protected BaseStore getStore() {
        return store;
    }

    /**
     * The object name that the MBean will be registered with the MBean Server
     * @return the MBeans object name
     */
    protected abstract ObjectName getMBeanName();

    // implementation of methods in the BaseStore interface

	public String getStoreName () {
		return store.getStoreName ();
	}

    /**
     * life cycle method for registering the MBean
     */
    public void start() {
        StoreManagerProxy.registerBean(getMBeanName(), this, true);
		generateNotification("Registering ObjectStore MBean");
//		store.start ();        
	}

    /**
     * life cycle method for un-registering the MBean
     */
    public void stop() {
//		store.stop ();
		generateNotification("Unregistering ObjectStore MBean");
        StoreManagerProxy.registerBean(getMBeanName(), this, false);
	}

	private void generateNotification(String message) {
		AttributeChangeNotification acn = new AttributeChangeNotification(this, 0, 0, message,
			"storeName", "String", "oldValue", "newValue");
		sendNotification(acn);
	}

	public MBeanNotificationInfo[] getNotificationInfo() {
		return new MBeanNotificationInfo[] {new MBeanNotificationInfo(
			new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE },
			AttributeChangeNotification.class.getName(),
			"Generated when the ObjectStore MBean is registered and destroyed")
		};
	}
}
