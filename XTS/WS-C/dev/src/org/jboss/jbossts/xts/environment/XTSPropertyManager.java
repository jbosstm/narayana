package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * class which ensures XTS properties are installed into the environment beans when they are created
 * and provides access to those beans
 */
public class XTSPropertyManager
{
    public static  WSCEnvironmentBean getWSCEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(WSCEnvironmentBean.class, xtsProperties);
    }

    public static WSCFEnvironmentBean getWSCFEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(WSCFEnvironmentBean.class, xtsProperties);
    }

    public static WSTEnvironmentBean getWSTEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(WSTEnvironmentBean.class, xtsProperties);
    }

    public static RecoveryEnvironmentBean getRecoveryEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(RecoveryEnvironmentBean.class, xtsProperties);
    }

    public static XTSEnvironmentBean getXTSEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(XTSEnvironmentBean.class, xtsProperties);
    }

    private static Properties mergeSystemProperties(Properties properties)
    {
        Properties systemProperties = System.getProperties();
        Enumeration<Object> propertyNames =  systemProperties.elements();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = (String) System.getProperty(propertyName);
            properties.setProperty(propertyName, propertyValue);
        }
        
        return properties;
    }

    static Properties xtsProperties;

    static {
        try {
            xtsProperties = XTSPropertiesFactory.getDefaultProperties();
        } catch (Exception e) {
            xtsProperties = new Properties(System.getProperties());
        }
    }
}
