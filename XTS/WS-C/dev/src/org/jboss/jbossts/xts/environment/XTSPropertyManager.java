/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

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
        return BeanPopulator.getDefaultInstance(WSCEnvironmentBean.class, xtsProperties);
    }

    public static WSCFEnvironmentBean getWSCFEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(WSCFEnvironmentBean.class, xtsProperties);
    }

    public static WSTEnvironmentBean getWSTEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(WSTEnvironmentBean.class, xtsProperties);
    }

    public static RecoveryEnvironmentBean getRecoveryEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class, xtsProperties);
    }

    public static XTSEnvironmentBean getXTSEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(XTSEnvironmentBean.class, xtsProperties);
    }

    static Properties xtsProperties = XTSPropertiesFactory.getDefaultProperties();
}
