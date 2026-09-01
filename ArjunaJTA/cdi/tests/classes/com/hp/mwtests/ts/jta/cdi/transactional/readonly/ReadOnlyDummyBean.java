/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.cdi.transactional.readonly;

import jakarta.transaction.Transactional;

/**
 * A Transactional bean visible to the WildFly deployment processor so the
 * Narayana transactional interceptors are activated for the deployment; the
 * annotation on {@link ReadOnlyBean} itself is only added by extension, which
 * the processor does not see (same trick as the stereotype extension tests).
 */
@Transactional
public class ReadOnlyDummyBean {
}
