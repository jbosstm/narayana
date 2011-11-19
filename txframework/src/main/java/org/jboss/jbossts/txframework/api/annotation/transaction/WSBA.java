package org.jboss.jbossts.txframework.api.annotation.transaction;

import org.jboss.jbossts.txframework.api.configuration.transaction.CompletionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class level annotation used to declare that  a participant service will participate in lifecycle
 * processing for WSBA business activity transactions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WSBA
{
    /**
     * the specific WSBA completion protocol which should be used for a WSBA service participant
     * @return
     */
    public CompletionType completionType() default CompletionType.COORDINATOR;
}