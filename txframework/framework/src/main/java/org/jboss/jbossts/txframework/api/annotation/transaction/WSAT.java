package org.jboss.jbossts.txframework.api.annotation.transaction;

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
}