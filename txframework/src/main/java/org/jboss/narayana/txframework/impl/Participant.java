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

package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Participant {

    protected ServiceInvocationMeta serviceInvocationMeta;
    protected Map<Class<? extends Annotation>, Method> lifecycleEventMap = new HashMap<Class<? extends Annotation>, Method>();
    protected List<Method> visibleMethods;

    private final Map txDataMap;

    public Participant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap) throws ParticipantRegistrationException {

        this.serviceInvocationMeta = serviceInvocationMeta;
        visibleMethods = getAllVisibleMethods(serviceInvocationMeta.getServiceClass());
        this.txDataMap = txDataMap;
    }

    public void resume() {

        TXDataMapImpl.resume(txDataMap);
    }

    public static void suspend() {

        TXDataMapImpl.suspend();
    }

    protected void registerEventsOfInterest(Class<? extends Annotation>... lifecycleEvents) {

        for (Class<? extends Annotation> lifecycleEvent : lifecycleEvents) {
            for (Method method : visibleMethods) {
                Annotation annotation = method.getAnnotation(lifecycleEvent);
                if (annotation != null) {
                    lifecycleEventMap.put(lifecycleEvent, method);
                }
            }
        }

    }

    protected Object invoke(Class<? extends Annotation> lifecycleEvent, Object... args) {

        resume();

        Method method = lifecycleEventMap.get(lifecycleEvent);
        if (method == null) {
            //No handler registered
            return null;
        }

        try {
            method.setAccessible(true);
            //todo: detect parameters better. Maybe have a different participant per interface.
            if (lifecycleEvent == ConfirmCompleted.class) {
                return method.invoke(serviceInvocationMeta.getProxyInstance(), args);
            } else {
                return method.invoke(serviceInvocationMeta.getProxyInstance());
            }
        } catch (Exception e) {
            //todo: Log stacktrace to error and throw a SystemException
            throw new RuntimeException("Unable to invoke method '" + method.getName() + "' on '" + serviceInvocationMeta.getServiceClass().getName() + "'", e);
        } finally {
            suspend();
        }
    }

    private List<Method> getAllVisibleMethods(Class clazz) {

        Method[] methods = clazz.getMethods();
        Method[] declaredMethods = clazz.getDeclaredMethods();

        List<Method> results = new LinkedList<Method>();

        for (Method m : methods) {
            if (!results.contains(m)) {
                results.add(m);
            }
        }
        for (Method m : declaredMethods) {
            if (!results.contains(m)) {
                results.add(m);
            }
        }
        return results;
    }
}
