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

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WSBACoordinatorCompletionParticipantTest {

    @Test
    public void testCallbacks() throws Exception {

        LifecycleImpl lifecycle = new LifecycleImpl();
        ServiceInvocationMeta serviceInvocationMeta = new ServiceInvocationMeta(lifecycle, LifecycleImpl.class, null);
        WSBACoordinatorCompletionParticipant participant = new WSBACoordinatorCompletionParticipant(serviceInvocationMeta, new HashMap(), null);

        participant.cancel();
        participant.close();
        participant.compensate();
        participant.complete();
        participant.confirmCompleted(true);
        participant.error();
        participant.status();
        participant.unknown();

        List<Class<? extends Annotation>> actualOrder = lifecycle.getExecutionOrder();
        assertOrder(actualOrder, Cancel.class, Close.class, Compensate.class, Complete.class, ConfirmCompleted.class, Error.class, Status.class, Unknown.class);
    }

    private void assertOrder(List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder) {

        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

    public class LifecycleImpl implements BusinessAgreementWithCoordinatorCompletionParticipant, ConfirmCompletedParticipant {

        private List<Class<? extends Annotation>> executionOrder = new ArrayList<Class<? extends Annotation>>();

        @Cancel
        public void cancel() {

            executionOrder.add(Cancel.class);
        }

        @Close
        public void close() {

            executionOrder.add(Close.class);
        }

        @Compensate
        public void compensate() {

            executionOrder.add(Compensate.class);
        }

        @Complete
        public void complete() {

            executionOrder.add(Complete.class);
        }

        @ConfirmCompleted
        public void confirmCompleted(boolean success) {

            executionOrder.add(ConfirmCompleted.class);
        }

        @Error
        public void error() {

            executionOrder.add(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
        }

        @Status
        public String status() {

            executionOrder.add(Status.class);
            return null;
        }

        @Unknown
        public void unknown() {

            executionOrder.add(Unknown.class);
        }

        public List<Class<? extends Annotation>> getExecutionOrder() {

            return executionOrder;
        }
    }
}
