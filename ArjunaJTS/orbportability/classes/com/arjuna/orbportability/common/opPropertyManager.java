/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the ORB Portability module.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class opPropertyManager
{
    public static OrbPortabilityEnvironmentBean getOrbPortabilityEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(OrbPortabilityEnvironmentBean.class);
    }
}