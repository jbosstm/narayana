/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for removing transaction logs that are considered too old.
 * <p>
 * <strong>Please note that removing transaction items can lead to a corrupted
 * system!</strong>
 *
 * @see BasicActionExpiryScanner
 */
public class AdvancedAtomicActionPurgeExpiryScanner extends BasicActionExpiryScanner {

    public AdvancedAtomicActionPurgeExpiryScanner() {
        super(new AtomicAction(), null);
    }
}
