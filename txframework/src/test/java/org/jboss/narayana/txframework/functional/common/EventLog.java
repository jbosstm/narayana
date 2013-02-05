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

package org.jboss.narayana.txframework.functional.common;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class EventLog implements Serializable {

    private static volatile List<Class<? extends Annotation>> dataUnavailableLog = new ArrayList<Class<? extends Annotation>>();
    private static volatile List<Class<? extends Annotation>> eventLog = new ArrayList<Class<? extends Annotation>>();

    public void addEvent(Class<? extends Annotation> event) {

        eventLog.add(event);
    }

    public void addDataUnavailable(Class<? extends Annotation> event) {

        dataUnavailableLog.add(event);
    }

    public List<Class<? extends Annotation>> getEventLog() {

        return eventLog;
    }

    public List<Class<? extends Annotation>> getDataUnavailableLog() {

        return dataUnavailableLog;
    }

    public void clear() {

        eventLog.clear();
        dataUnavailableLog.clear();
    }

    public static String asString(List<Class<? extends Annotation>> events) {

        String result = "";

        for (Class<? extends Annotation> clazz : events) {
            result += clazz.getSimpleName() + ",";
        }
        return result;
    }
}
