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
package org.jboss.jbossts.txbridge.tests.inbound.utility;

import org.apache.log4j.Logger;

import javax.transaction.Synchronization;

/**
 * Implementation of Synchronization for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestSynchronization implements Synchronization
{
    private static Logger log = Logger.getLogger(TestSynchronization.class);

    private boolean beforeCompletionDone = false;
    private boolean afterCompletionDone = false;

    private boolean failInBeforeCompletion = false;

    public boolean isBeforeCompletionDone()
    {
        return beforeCompletionDone;
    }

    public boolean isAfterCompletionDone()
    {
        return afterCompletionDone;
    }

    public boolean isFailInBeforeCompletion()
    {
        return failInBeforeCompletion;
    }

    public void setFailInBeforeCompletion(boolean failInBeforeCompletion)
    {
        this.failInBeforeCompletion = failInBeforeCompletion;
    }

    public void beforeCompletion() {
        if(beforeCompletionDone) {
            log.trace("beforeCompletion called more than once");
            throw new RuntimeException("beforeCompletion called more than once");
        }

        beforeCompletionDone = true;
        log.trace("TestSynchronization.beforeCompletion()");

        if(failInBeforeCompletion) {
            log.trace("failing in beforeCompletion");
            throw new RuntimeException("failed in beforeCompletion");
        }
    }

    public void afterCompletion(int i) {
        if(afterCompletionDone) {
            log.trace("afterCompletion called more than once");
            throw new RuntimeException("afterCompletion called more than once");
        }

        afterCompletionDone = true;
        log.trace("TestSynchronization.afterCompletion("+i+")");
    }
}