/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.compensations.internal;


import org.jboss.narayana.compensations.api.CompensationManager;

/**
 * @author paul.robinson@redhat.com 24/04/2013
 */
public class CompensationManagerImpl implements CompensationManager {

    private static final ThreadLocal<CompensationManagerState> compensationManagerStateThreadLocal = new ThreadLocal<CompensationManagerState>();

    /**
     * Mark the transaction as "compensate only". This ensures that the compensation-based transaction will be cancelled and any
     * completed work compensated.
     */
    @Override
    public void setCompensateOnly() {

        CompensationManagerState compensationManagerState = compensationManagerStateThreadLocal.get();
        compensationManagerState.setCompensateOnly(true);
    }

    /**
     * Check if compensating transaction was marked as compensate-only.
     *
     * @return {@code true} if the transaction was marked as compensate-only and {@code false} otherwise.
     */
    public static boolean isCompensateOnly() {

        if (compensationManagerStateThreadLocal.get() == null) {
            return false;
        }
        return compensationManagerStateThreadLocal.get().isCompensateOnly();
    }

    /**
     * Resume compensation manager by associating it with the current thread.
     *
     * @param compensationManagerState state of the compensation manager.
     */
    public static void resume(CompensationManagerState compensationManagerState) {

        compensationManagerStateThreadLocal.set(compensationManagerState);
    }

    /**
     * Suspend compensation manager by disassociating it from the current thread.
     *
     * @return state of the suspended compensation manager.
     */
    public static CompensationManagerState suspend() {

        CompensationManagerState result = compensationManagerStateThreadLocal.get();
        compensationManagerStateThreadLocal.set(null);
        return result;
    }

}
