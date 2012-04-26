package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.api.annotation.management.DataManagement;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Participant
{
    protected Object serviceImpl;
    protected Map<Class<? extends Annotation>, Method> lifecycleEventMap = new HashMap<Class<? extends Annotation>, Method>();
    protected List<Method> visibleMethods;

    private final Map txDataMap = new HashMap();

    public Participant(Object serviceImpl, boolean injectDataManagement) throws ParticipantRegistrationException
    {
        this.serviceImpl = serviceImpl;
        visibleMethods = getAllVisibleMethods(serviceImpl.getClass());

        if (injectDataManagement)
        {
            injectTxDataMap(txDataMap, serviceImpl);
        }
    }

    private void injectTxDataMap(Map txDataMap, Object serviceImpl) throws ParticipantRegistrationException
    {
        for (Field field : serviceImpl.getClass().getDeclaredFields())
        {
            if (field.getAnnotation(DataManagement.class) != null)
            {
                try
                {
                    if (!field.getType().equals(Map.class))
                    {
                        throw new ParticipantRegistrationException("Unable to inject data management Map into to field '" + field.getName() + "' on '" + serviceImpl.getClass().getName() +
                                "': Field is not of type '" + Map.class + "'");
                    }
                    field.setAccessible(true);
                    field.set(serviceImpl, txDataMap);
                }
                catch (IllegalAccessException e)
                {
                    throw new ParticipantRegistrationException("Unable to inject data management map impl to field '" + field.getName() + "' on '" + serviceImpl.getClass().getName() + "'", e);
                }
            }
        }
        //didn't find an injection point. No problem as this is optional
    }

    protected void registerEventsOfInterest(Class<? extends Annotation>... lifecycleEvents)
    {
        for (Class<? extends Annotation> lifecycleEvent : lifecycleEvents)
        {
            for (Method method : visibleMethods)
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
            method.setAccessible(true);
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

    private List<Method> getAllVisibleMethods(Class clazz)
    {
        Method[] methods = clazz.getMethods();
        Method[] declaredMethods = clazz.getDeclaredMethods();

        List<Method> results = new LinkedList<Method>();

        for (Method m : methods)
        {
            if (!results.contains(m))
            {
                results.add(m);
            }
        }
        for (Method m : declaredMethods)
        {
            if (!results.contains(m))
            {
                results.add(m);
            }
        }
        return results;
    }
}
