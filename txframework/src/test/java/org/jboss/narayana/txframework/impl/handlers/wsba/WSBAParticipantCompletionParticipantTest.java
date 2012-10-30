package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Status;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Unknown;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WSBAParticipantCompletionParticipantTest
{
    @Test
    public void testCallbacks() throws Exception
    {
        LifecycleImpl lifecycle = new LifecycleImpl();
        ServiceInvocationMeta serviceInvocationMeta = new ServiceInvocationMeta(lifecycle, LifecycleImpl.class, null);
        WSBAParticipantCompletionParticipant participant = new WSBAParticipantCompletionParticipant(serviceInvocationMeta, new HashMap(), null);

        participant.cancel();
        participant.close();
        participant.compensate();
        participant.confirmCompleted(true);
        participant.error();
        participant.status();
        participant.unknown();

        List<Class<? extends Annotation>> expectedOrder = new ArrayList<Class<? extends Annotation>>();
        expectedOrder.add(Cancel.class);
        expectedOrder.add(Close.class);
        expectedOrder.add(Compensate.class);
        expectedOrder.add(ConfirmCompleted.class);
        expectedOrder.add(Error.class);
        expectedOrder.add(Status.class);
        expectedOrder.add(Unknown.class);

        List<Class<? extends Annotation>> actualOrder = lifecycle.getExecutionOrder();
        assertOrder(actualOrder, Cancel.class, Close.class, Compensate.class, ConfirmCompleted.class, Error.class, Status.class, Unknown.class);
    }

    private void assertOrder(List<Class<? extends Annotation>> actualOrder, Class<? extends Annotation>... expectedOrder)
    {
        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), actualOrder);
    }

    public class LifecycleImpl implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant
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

        @ConfirmCompleted
        public void confirmCompleted(boolean success)
        {
            executionOrder.add(ConfirmCompleted.class);
        }

        @Error
        public void error()
        {
            executionOrder.add(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
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
