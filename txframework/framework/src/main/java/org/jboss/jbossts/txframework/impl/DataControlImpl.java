package org.jboss.jbossts.txframework.impl;

import org.jboss.jbossts.txframework.api.management.DataControl;
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
