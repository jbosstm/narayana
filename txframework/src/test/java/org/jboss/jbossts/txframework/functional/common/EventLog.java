package org.jboss.jbossts.txframework.functional.common;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class EventLog
{
    private static volatile List<Class<? extends Annotation>> log = new ArrayList<Class<? extends Annotation>>();

    public void add(Class<? extends Annotation> event)
    {
        log.add(event);
    }

    public List<Class<? extends Annotation>> getLog()
    {
        return log;
    }

    public void clear()
    {
        log.clear();
    }
}
