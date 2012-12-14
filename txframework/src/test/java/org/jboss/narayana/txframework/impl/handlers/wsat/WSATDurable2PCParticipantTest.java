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

package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WSATDurable2PCParticipantTest {

    @Test
    public void testCallbacks() throws Exception {

        LifecycleImpl lifecycle = new LifecycleImpl();
        ServiceInvocationMeta serviceInvocationMeta = new ServiceInvocationMeta(lifecycle, LifecycleImpl.class, null);
        WSATDurable2PCParticipant participant = new WSATDurable2PCParticipant(serviceInvocationMeta, new HashMap());

        participant.prepare();
        participant.commit();
        participant.rollback();
        participant.unknown();
        participant.error();

        List<Class<? extends Annotation>> actualOrder = lifecycle.getExecutionOrder();
        assertOrder(actualOrder, Prepare.class, Commit.class, Rollback.class, Unknown.class, Error.class);
    }

    public class LifecycleImpl implements Durable2PCParticipant {

        private List<Class<? extends Annotation>> executionOrder = new ArrayList<Class<? extends Annotation>>();

        @Prepare
        public Vote prepare() throws WrongStateException, SystemException {

            executionOrder.add(Prepare.class);
            //todo: return a vote
            return null;
        }

        @Commit
        public void commit() throws WrongStateException, SystemException {

            executionOrder.add(Commit.class);
        }

        @Rollback
        public void rollback() throws WrongStateException, SystemException {

            executionOrder.add(Rollback.class);
        }

        @Unknown
        public void unknown() throws SystemException {

            executionOrder.add(Unknown.class);
        }

        @Error
        public void error() throws SystemException {

            executionOrder.add(Error.class);
        }

        public List<Class<? extends Annotation>> getExecutionOrder() {

            return executionOrder;
        }
    }

    private void assertOrder(List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder) {

        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }
}
