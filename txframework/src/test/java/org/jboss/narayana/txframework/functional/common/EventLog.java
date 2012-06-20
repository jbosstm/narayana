package org.jboss.narayana.txframework.functional.common;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class EventLog implements Serializable
{
    private static volatile List<Class<? extends Annotation>> dataUnavailableLog = new ArrayList<Class<? extends Annotation>>();
    private static volatile List<Class<? extends Annotation>> eventLog = new ArrayList<Class<? extends Annotation>>();

    public void addEvent(Class<? extends Annotation> event)
    {
        eventLog.add(event);
    }

    public void addDataUnavailable(Class<? extends Annotation> event)
    {
        dataUnavailableLog.add(event);
    }

    public List<Class<? extends Annotation>> getEventLog()
    {
        return eventLog;
    }

    public List<Class<? extends Annotation>> getDataUnavailableLog()
    {
        return dataUnavailableLog;
    }

    public void clear()
    {
        eventLog.clear();
        dataUnavailableLog.clear();
    }

    public static String asString(List<Class<? extends Annotation>> events)
    {
        String result = "";

        for (Class<? extends Annotation> clazz : events)
        {
            result += clazz.getSimpleName() + ",";
        }
        return result;
    }
}
