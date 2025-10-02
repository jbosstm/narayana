package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.*;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBACoordinatorCompletionParticipant;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WSBACoordinatorCompletionParticipantTest
{
    @Test
    public void testCallbacks() throws Exception
    {
        LifecycleImpl lifecycle = new LifecycleImpl();
        WSBACoordinatorCompletionParticipant participant = new WSBACoordinatorCompletionParticipant(lifecycle);

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

    private void assertOrder(List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

    public class LifecycleImpl implements BusinessAgreementWithCoordinatorCompletionParticipant, ConfirmCompletedParticipant
    {
        private List<Class<? extends Annotation>> executionOrder = new ArrayList<Class<? extends Annotation>>();

        @Cancel
        public void cancel()
        {
            executionOrder.add(Cancel.class);
        }

        @Close
        public void close()
        {
            executionOrder.add(Close.class);
        }

        @Compensate
        public void compensate()
        {
            executionOrder.add(Compensate.class);
        }

        @Complete
        public void complete()
        {
            executionOrder.add(Complete.class);
        }

        @ConfirmCompleted
        public void confirmCompleted(boolean success)
        {
            executionOrder.add(ConfirmCompleted.class);
        }

        @Error
        public void error()
        {
            executionOrder.add(org.jboss.narayana.txframework.api.annotation.lifecycle.wsba.Error.class);
        }

        @Status
        public String status()
        {
            executionOrder.add(Status.class);
            return null;
        }

        @Unknown
        public void unknown()
        {
            executionOrder.add(Unknown.class);
        }

        public List<Class<? extends Annotation>> getExecutionOrder()
        {
            return executionOrder;
        }
    }
}
