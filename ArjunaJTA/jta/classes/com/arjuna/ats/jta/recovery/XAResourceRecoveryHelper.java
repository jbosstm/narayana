/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.recovery;

import javax.transaction.xa.XAResource;

/**
 * Interface to be implemented by external entities that instantiate
 * and supply their own XAResources to the recovery system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface XAResourceRecoveryHelper {

    public boolean initialise(String p) throws Exception;

    public XAResource[] getXAResources() throws Exception;
}