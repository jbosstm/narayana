package org.jboss.jbossts.txframework.api.management;

/**
 * Interface defining a data control object which can be injected into a framework web service or
 * lifecycle method. Instances of this type and its subtypes can be uses to type fields which are
 * the target of a DataManagement attribute.
 */
public interface DataControl
{
    public void put(Object objectId, Object object);
    public Object get(Object objectId);
}
