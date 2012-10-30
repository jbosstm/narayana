package org.jboss.narayana.txframework.api.exception;

/**
 * @Author paul.robinson@redhat.com 02/11/2012
 */
public class TransactionDataUnavailableException extends TXFrameworkRuntimeException {

    public TransactionDataUnavailableException(String message) {
        super(message);
    }

    public TransactionDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
