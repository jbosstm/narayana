/*
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2014
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.arjuna.recovery;

/**
 * Exposes the current status of the periodic recovery thread as reported by {@link RecoveryManagerImple#trySuspendScan(boolean)}
 */
public enum RecoveryManagerStatus {
    /**
     * state value indicating that new scans may proceed ({@link PeriodicRecovery.Mode#ENABLED})
     */
    ENABLED,
    /**
     * state value indicating that new scans may not proceed and the periodic recovery thread should
     * suspend ({@link PeriodicRecovery.Mode#SUSPENDED})
     */
    SUSPENDED,
    /**
     * state value indicating that new scans may not proceed and that the singleton
     * PeriodicRecovery thread instance should exit if it is still running ({@link PeriodicRecovery.Mode#TERMINATED})
     */
    TERMINATED;
}
