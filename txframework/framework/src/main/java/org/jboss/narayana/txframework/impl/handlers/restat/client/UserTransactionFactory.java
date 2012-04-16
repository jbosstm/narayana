package org.jboss.narayana.txframework.impl.handlers.restat.client;

/**
 * @author paul.robinson@redhat.com, 2012-04-12
 */
public class UserTransactionFactory {

    public static UserTransaction userTransaction()
    {
        return new UserTransaction();
    }

}
