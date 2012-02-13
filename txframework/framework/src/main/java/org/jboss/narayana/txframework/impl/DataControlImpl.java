package org.jboss.narayana.txframework.impl;

import org.jboss.narayana.txframework.api.management.DataControl;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

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
