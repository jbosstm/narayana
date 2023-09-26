/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
    /*
     * Note to implementers:
     * if named instances of the CoreEnvironmentBean are created then
     * you must apply the same technique as is used in the implementation
     * of getObjectStoreEnvironmentBean method below
     */
    public static CoreEnvironmentBean getCoreEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class);
    }

    /*
     * Note to implementers:
     * if named instances of the CoordinatorEnvironmentBean are created then
     * you must apply the same technique as is used in the implementation
     * of getObjectStoreEnvironmentBean method below
     */
    public static CoordinatorEnvironmentBean getCoordinatorEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class);
    }

    /**
     * @return an ObjectStoreEnvironmentBean instance that will update all named
     * instances of beans of type ObjectStoreEnvironmentBean as returned from calls
     * to {@link BeanPopulator#getNamedInstance(Class, String)} and
     * {@link BeanPopulator#getDefaultInstance(Class)}.
     *
     * The getters of the returned object will pass through to the getter of
     * the default bean instance that is returned from a call to
     * {@link BeanPopulator#getDefaultInstance(Class)}.
     */
    public static ObjectStoreEnvironmentBean getObjectStoreEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(MetaObjectStoreEnvironmentBean.class);
    }
}