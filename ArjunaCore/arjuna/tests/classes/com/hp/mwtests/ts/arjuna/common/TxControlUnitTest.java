/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.TxControl;

public class TxControlUnitTest
{
    @Test
    public void testStartStop() throws Exception
    {
        TxControl.enable();

        assertTrue(TxControl.isEnabled());

        TxControl.disable(true);

        assertFalse(TxControl.isEnabled());

        TxControl.enable();

        assertTrue(TxControl.isEnabled());
    }
}