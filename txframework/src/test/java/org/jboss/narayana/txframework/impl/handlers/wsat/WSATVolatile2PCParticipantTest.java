package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.PostCommit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.PrePrepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Unknown;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WSATVolatile2PCParticipantTest
{
    @Test
    public void testCallbacks() throws Exception
    {
        LifecycleImpl lifecycle = new LifecycleImpl();
        ServiceInvocationMeta serviceInvocationMeta = new ServiceInvocationMeta(lifecycle, LifecycleImpl.class, null);
        WSATVolatile2PCParticipant participant = new WSATVolatile2PCParticipant(serviceInvocationMeta, new HashMap(), null);

        participant.prepare();
        participant.commit();
        participant.rollback();
        participant.unknown();
        participant.error();

        List<Class<? extends Annotation>> actualOrder = lifecycle.getExecutionOrder();
        assertOrder(actualOrder, PrePrepare.class, PostCommit.class, Rollback.class, Unknown.class, Error.class);
    }

    private void assertOrder(List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

    public class LifecycleImpl implements Volatile2PCParticipant
    {
        private List<Class<? extends Annotation>> executionOrder = new ArrayList<Class<? extends Annotation>>();

        @PrePrepare
        public Vote prepare() throws WrongStateException, SystemException
        {
            executionOrder.add(PrePrepare.class);
            //todo: return a vote
            return null;
        }

        @PostCommit
        public void commit() throws WrongStateException, SystemException
        {
            executionOrder.add(PostCommit.class);
        }

        @Rollback
        public void rollback() throws WrongStateException, SystemException
        {
            executionOrder.add(Rollback.class);
        }

        @Unknown
        public void unknown() throws SystemException
        {
            executionOrder.add(Unknown.class);
        }

        @Error
        public void error() throws SystemException
        {
            executionOrder.add(Error.class);
        }

        public List<Class<? extends Annotation>> getExecutionOrder()
        {
            return executionOrder;
        }
    }
}
