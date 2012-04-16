package org.jboss.narayana.txframework.impl.handlers.restat.client;

/**
 * @author paul.robinson@redhat.com, 2012-04-12
 */
public class TransactionRolledBackException extends Exception {

    public TransactionRolledBackException() {
    }

    public TransactionRolledBackException(String message) {
        super(message);
    }

    public TransactionRolledBackException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionRolledBackException(Throwable cause) {
        super(cause);
    }
}
