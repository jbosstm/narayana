package org.jboss.narayana.txframework.impl.handlers;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;

import javax.interceptor.InvocationContext;

public interface ProtocolHandler
{
    public Object proceed(InvocationContext ic) throws Exception;

    public void notifySuccess() throws TXFrameworkException;

    public void notifyFailure() throws TXFrameworkException;
}
