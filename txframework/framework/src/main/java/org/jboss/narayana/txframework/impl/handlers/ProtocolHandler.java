package org.jboss.narayana.txframework.impl.handlers;

import javax.interceptor.InvocationContext;

public interface ProtocolHandler
{
    public Object proceed(InvocationContext ic) throws Exception;
}
