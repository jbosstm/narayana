/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.common.internal.util.logging;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * This class is used to control properties values for CLF.
 *
 * The properties can originate either from a configuration file (by default common.properties)
 * or it can be primed by using the class directly from an embedding product (such as
 * the JBoss Transaction Service).
 *
 * The default file location can be overridden by setting the property
 * "com.arjuna.common.util.logging.propertiesFile"
 *
 * @author Malik SAHEB
 */
public class commonPropertyManager
{
    public static LoggingEnvironmentBean getLoggingEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(LoggingEnvironmentBean.class);
    }

    public static DefaultLogEnvironmentBean getDefaultLogEnvironmentBean()
    {
        return BeanPopulator.getSingletonInstance(DefaultLogEnvironmentBean.class);
    }
}
