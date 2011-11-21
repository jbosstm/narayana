package org.jboss.jbossts.txframework.impl;

import com.arjuna.wst.Durable2PCParticipant;
import org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba.ConfirmCompleted;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class Participant
{
    protected Object serviceImpl;
    protected Map<Class<? extends Annotation>, Method> lifecycleEventMap = new HashMap<Class<? extends Annotation>, Method>();

    public Participant(Object serviceImpl)
    {
        this.serviceImpl = serviceImpl;
    }

    protected void registerEventsOfInterest(Class<? extends Annotation>... lifecycleEvents)
    {
        for (Class<? extends Annotation> lifecycleEvent : lifecycleEvents)
        {
            for (Method method : serviceImpl.getClass().getMethods())
            {
                Annotation annotation = method.getAnnotation(lifecycleEvent);
                if (annotation != null)
                {
                    lifecycleEventMap.put(lifecycleEvent, method);
                }
            }
        }

    }

    protected Object invoke(Class<? extends Annotation> lifecycleEvent, Object... args)
    {
        Method method = lifecycleEventMap.get(lifecycleEvent);
        if (method == null)
        {
            //No handler registered
            return null;
        }

        try
        {
            //todo: detect parameters better. Maybe have a different participant per interface.
            if (lifecycleEvent == ConfirmCompleted.class)
            {
                return method.invoke(serviceImpl, args);
            }
            else
            {
                return method.invoke(serviceImpl);
            }
        }
        catch (Exception e)
        {
            //todo: Log stacktrace to debug and throw a SystemException
            throw new RuntimeException("Unable to invoke method '" + method.getName() + "' on '" + serviceImpl.getClass().getName() + "'", e);
        }
    }
}
