/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.subordinate;

import javax.transaction.Synchronization;

/**
 * Implementation of Synchronization for use in tx test cases.
 */
public class TestSynchronization implements Synchronization
{
    private boolean beforeCompletionDone = false;
    private boolean afterCompletionDone = false;

    public boolean isBeforeCompletionDone()
    {
        return beforeCompletionDone;
    }

    public boolean isAfterCompletionDone()
    {
        return afterCompletionDone;
    }

    public void beforeCompletion() {
        if(beforeCompletionDone) {
            System.out.println("beforeCompletion called more than once");
            throw new RuntimeException("beforeCompletion called more than once");
        }

        beforeCompletionDone = true;
        System.out.println("TestSynchronization.beforeCompletion()");
    }

    public void afterCompletion(int i) {
        if(afterCompletionDone) {
            System.out.println("afterCompletion called more than once");
            throw new RuntimeException("afterCompletion called more than once");
        }

        afterCompletionDone = true;
        System.out.println("TestSynchronization.afterCompletion("+i+")");
    }
}