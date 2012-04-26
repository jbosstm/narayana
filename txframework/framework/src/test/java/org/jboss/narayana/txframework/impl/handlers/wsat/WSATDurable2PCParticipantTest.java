package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Unknown;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WSATDurable2PCParticipantTest
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
        assertOrder(actualOrder, Prepare.class, Commit.class, Rollback.class, Unknown.class, Error.class);
    }

    public class LifecycleImpl implements Durable2PCParticipant
    {
        private List<Class<? extends Annotation>> executionOrder = new ArrayList<Class<? extends Annotation>>();

        @Prepare
        public Vote prepare() throws WrongStateException, SystemException
        {
            executionOrder.add(Prepare.class);
            //todo: return a vote
            return null;
        }

        @Commit
        public void commit() throws WrongStateException, SystemException
        {
            executionOrder.add(Commit.class);
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
    private void assertOrder( List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }
}
