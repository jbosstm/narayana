package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.management.DataControl;

import java.util.HashMap;
import java.util.Map;

/*
    Todo: this should store data keyed against the associated transaction.
        puts and gets are then done based on the current TX on the thread.
 */

public class DataControlImpl implements DataControl
{
    private Map<Object, Object> map = new HashMap<Object, Object>();

    public void put(Object objectId, Object object)
    {
        map.put(objectId, object);
    }

    public Object get(Object objectId)
    {
        return map.get(objectId);
    }
}
