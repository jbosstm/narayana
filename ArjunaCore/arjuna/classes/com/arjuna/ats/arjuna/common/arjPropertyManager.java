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

import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the Arjuna module.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class arjPropertyManager
{
    public static CoreEnvironmentBean getCoreEnvironmentBean()
    {
        try
        {
            return BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class);
        }
        catch (final java.lang.RuntimeException ex)
        {
            /*
             * See JBTM-1227
             * 
             * Once the XML parsing is updated, then this should no longer
             * be necessary.
             */
            
            if (Utility.isAndroid())
                return new CoreEnvironmentBean();  // todo android
            else
                throw ex;
        }
    }

    public static CoordinatorEnvironmentBean getCoordinatorEnvironmentBean()
    {
        try
        {
            return BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class);
        }
        catch (final java.lang.RuntimeException ex)
        {
            if (Utility.isAndroid())
                return new CoordinatorEnvironmentBean();  // todo android
            else
                throw new RuntimeException(ex);
        }
    }

    public static ObjectStoreEnvironmentBean getObjectStoreEnvironmentBean()
    {
        try
        {
            return BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class);
        }
        catch (final java.lang.RuntimeException ex)
        {
            if (Utility.isAndroid())
                return new ObjectStoreEnvironmentBean();  // todo android
            else
                throw new RuntimeException(ex);
        }
    }
}
