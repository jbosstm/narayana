package org.jboss.jbossts.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import junit.framework.Assert;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsat.*;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsat.Error;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WSATVolatile2PCParticipantTest
{
    @Test
    public void testCallbacks() throws Exception
    {
        LifecycleImpl lifecycle = new LifecycleImpl();
        WSATDurable2PCParticipant participant = new WSATDurable2PCParticipant(lifecycle);

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

        @Prepare
        public Vote prepare() throws WrongStateException, SystemException
        {
            executionOrder.add(PrePrepare.class);
            //todo: return a vote
            return null;
        }

        @Commit
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
