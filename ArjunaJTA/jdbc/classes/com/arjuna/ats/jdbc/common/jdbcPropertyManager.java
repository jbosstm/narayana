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
 * $Id: jdbcPropertyManager.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.jdbc.common;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Property manager wrapper for the JDBC module.
 */
public class jdbcPropertyManager
{
    /**
     * @deprecated use JDBCEnvironmentBean instead
     * @return
     */
    @Deprecated
    public static PropertyManager getPropertyManager()
    {
        return PropertyManagerFactory.getPropertyManagerForModule("jdbc", Environment.PROPERTIES_FILE);
    }

    public static JDBCEnvironmentBean getJDBCEnvironmentBean()
    {
        synchronized (jdbcEnvironmentBeanInit) {
            if(!jdbcEnvironmentBeanInit.get()) {
                try {
                    BeanPopulator.configureFromPropertyManager(jdbcEnvironmentBean,  getPropertyManager());
                    jdbcEnvironmentBeanInit.set(true);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return jdbcEnvironmentBean;
    }

    private static final AtomicBoolean jdbcEnvironmentBeanInit = new AtomicBoolean(false);
    private static final JDBCEnvironmentBean jdbcEnvironmentBean = new JDBCEnvironmentBean();

}
