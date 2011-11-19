package org.jboss.jbossts.txframework.impl.handlers;

import javax.interceptor.InvocationContext;

public interface ProtocolHandler
{
    public Object proceed(InvocationContext ic) throws Exception;
}
