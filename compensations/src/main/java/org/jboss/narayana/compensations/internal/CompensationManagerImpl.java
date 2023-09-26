/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;


import org.jboss.narayana.compensations.api.CompensationManager;

/**
 * @author paul.robinson@redhat.com 24/04/2013
 */
public class CompensationManagerImpl implements CompensationManager {

    private static final ThreadLocal<CompensationManagerState> compensationManagerStateThreadLocal = new ThreadLocal<CompensationManagerState>();

    @Override
    public void setCompensateOnly() {

        CompensationManagerState compensationManagerState = compensationManagerStateThreadLocal.get();
        compensationManagerState.setCompensateOnly(true);
    }

    public static boolean isCompensateOnly() {

        if (compensationManagerStateThreadLocal.get() == null) {
            return false;
        }
        return compensationManagerStateThreadLocal.get().isCompensateOnly();
    }

    public static void resume(CompensationManagerState compensationManagerState) {

        compensationManagerStateThreadLocal.set(compensationManagerState);
    }

    public static CompensationManagerState suspend() {

        CompensationManagerState result = compensationManagerStateThreadLocal.get();
        compensationManagerStateThreadLocal.set(null);
        return result;
    }

}