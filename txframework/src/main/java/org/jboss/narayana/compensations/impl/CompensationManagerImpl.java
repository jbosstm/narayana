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

package org.jboss.narayana.compensations.impl;


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
