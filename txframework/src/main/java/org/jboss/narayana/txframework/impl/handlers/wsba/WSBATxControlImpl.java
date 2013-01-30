/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.txframework.api.exception.TXControlRuntimeException;
import org.jboss.narayana.txframework.api.management.WSBATxControl;

public class WSBATxControlImpl implements WSBATxControl {

    private static final ThreadLocal<BAParticipantManager> baParticipantManagerThreadLocal = new ThreadLocal<BAParticipantManager>();

    //todo: Need to hook into lifecycle or record it here.
    static final ThreadLocal<Boolean> cannotCompleteThreadLocal = new ThreadLocal<Boolean>();

    public WSBATxControlImpl() {

    }


    public static void resume(BAParticipantManager baParticipantManager) {

        baParticipantManagerThreadLocal.set(baParticipantManager);
        cannotCompleteThreadLocal.set(false);
    }

    public static void suspend() {

        baParticipantManagerThreadLocal.remove();
    }

    public void exit() throws TXControlRuntimeException {

        try {
            baParticipantManagerThreadLocal.get().exit();
        } catch (Exception e) {
            throw new TXControlRuntimeException("Exception when calling 'exit' on participant manager", e);
        }
    }

    public void cannotComplete() throws TXControlRuntimeException {

        try {
            baParticipantManagerThreadLocal.get().cannotComplete();
            cannotCompleteThreadLocal.set(true);
        } catch (Exception e) {
            throw new TXControlRuntimeException("Exception when calling 'cannotComplete' on participant manager", e);
        }
    }

    public void completed() throws TXControlRuntimeException {

        try {
            baParticipantManagerThreadLocal.get().completed();
        } catch (Exception e) {
            throw new TXControlRuntimeException("Exception when calling 'completed' on participant manager", e);
        }
    }

    public void readOnly() throws TXControlRuntimeException {

        try {
            baParticipantManagerThreadLocal.get().exit();
        } catch (Exception e) {
            throw new TXControlRuntimeException("Exception when calling 'exit' on participant manager", e);
        }
    }

    public void fail() throws TXControlRuntimeException {

        try {
            //todo: Why does this take a QName?
            baParticipantManagerThreadLocal.get().fail(null);
        } catch (Exception e) {
            throw new TXControlRuntimeException("Exception when calling 'fail' on participant manager", e);
        }
    }

    public boolean isCannotComplete() {

        return cannotCompleteThreadLocal.get();
    }
}
