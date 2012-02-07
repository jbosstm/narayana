package org.jboss.narayana.txframework.api.annotation.transaction;

import org.jboss.narayana.txframework.api.configuration.BridgeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class level annotation used to declare that  a participant service will participate in lifecycle
 * processing for WSAT atomic transactions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WSAT
{
    /**
     * attribute specifying whether automatic bridging to a JTA transaction should occur when a service
     * request or transactional lifecyle handler (CLOSE or COMPENSATE) is first called in an enclosing
     * WS or REST transaction
     */
    public BridgeType bridgeType() default BridgeType.DEFAULT;

}