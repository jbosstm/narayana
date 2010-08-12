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
        InputStream is = null;
        try {
            is = XTSPropertyManager.class.getClassLoader().getResourceAsStream("xts-properties.xml");
            Properties properties = new Properties();
            properties.loadFromXML(is);
            xtsProperties = mergeSystemProperties(properties);
        } catch (Exception e) {
            // ok, so we have no property file!
            // just rely on system properties
            xtsProperties = System.getProperties();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
