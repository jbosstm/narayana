/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for moving transaction logs that are considered too old to an
 * expiry folder.
 *
 * @see BasicActionExpiryScanner
 */
public class AdvancedAtomicActionExpiryScanner extends BasicActionExpiryScanner {

    public AdvancedAtomicActionExpiryScanner() {
        super(new AtomicAction(), "/Expired");
    }
}
