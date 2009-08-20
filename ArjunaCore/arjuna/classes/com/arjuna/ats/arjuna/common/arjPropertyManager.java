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
/*
 * Copyright (C) 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: arjPropertyManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.common;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Property manager wrapper for the Arjuna module.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class arjPropertyManager
{
    /**
     * @deprecated use environment beans instead.
     * @return
     */
    @Deprecated
    public static PropertyManager getPropertyManager()
    {
        return PropertyManagerFactory.getPropertyManagerForModule("arjuna", Environment.PROPERTIES_FILE);
    }

    public static CoreEnvironmentBean getCoreEnvironmentBean()
    {
        synchronized (coreEnvironmentBeanInit) {
            if(!coreEnvironmentBeanInit.get()) {
                try {
                    BeanPopulator.configureFromPropertyManager(coreEnvironmentBean,  getPropertyManager());
                    coreEnvironmentBeanInit.set(true);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return coreEnvironmentBean;
    }

    private static final AtomicBoolean coreEnvironmentBeanInit = new AtomicBoolean(false);
    private static final CoreEnvironmentBean coreEnvironmentBean = new CoreEnvironmentBean();

    public static CoordinatorEnvironmentBean getCoordinatorEnvironmentBean()
    {
        synchronized (coordinatorEnvironmentBeanInit) {
            if(!coordinatorEnvironmentBeanInit.get()) {
                try {
                    BeanPopulator.configureFromPropertyManager(coordinatorEnvironmentBean,  getPropertyManager());
                    coordinatorEnvironmentBeanInit.set(true);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return coordinatorEnvironmentBean;
    }

    private static final AtomicBoolean coordinatorEnvironmentBeanInit = new AtomicBoolean(false);
    private static final CoordinatorEnvironmentBean coordinatorEnvironmentBean = new CoordinatorEnvironmentBean();

    public static ObjectStoreEnvironmentBean getObjectStoreEnvironmentBean()
    {
        synchronized (objectStoreEnvironmentBeanInit) {
            if(!objectStoreEnvironmentBeanInit.get()) {
                try {
                    BeanPopulator.configureFromPropertyManager(objectStoreEnvironmentBean, getPropertyManager());
                    objectStoreEnvironmentBeanInit.set(true);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return objectStoreEnvironmentBean;
    }

    private static final AtomicBoolean objectStoreEnvironmentBeanInit = new AtomicBoolean(false);
    private static final ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();

}
