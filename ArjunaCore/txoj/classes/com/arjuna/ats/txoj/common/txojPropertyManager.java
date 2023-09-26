/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.txoj.common;

import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the TXOJ module.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class txojPropertyManager
{
    public static TxojEnvironmentBean getTxojEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(TxojEnvironmentBean.class);
    }
}