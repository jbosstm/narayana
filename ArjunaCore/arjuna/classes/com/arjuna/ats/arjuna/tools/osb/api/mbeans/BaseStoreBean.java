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
