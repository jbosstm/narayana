package org.jboss.narayana.txframework.impl.handlers.restat.client;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @Author paul.robinson@redhat.com 09/04/2012
 *
 * Quick and dirty implementation of a TX annotation for REST-AT, similar to the EJB tx annotations. Needs doing properly and supporting all variations
 */
@Required
@Interceptor
public class RestTXRequiredInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        UserTransaction userTransaction = UserTransactionFactory.userTransaction();
        userTransaction.begin();

        //todo: check for exception and rollback if occurs
        Object result = ic.proceed();

        userTransaction.commit();

        return result;
    }


}
