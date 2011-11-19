package org.jboss.jbossts.txframework.api.annotation.lifecycle.wsba;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    //todo: update docs on this and the other similarly cloned annotations
    Method level annotation stating that this @ServiceInvocation method should Complete a ParticipantCompletion WS-BA
    Transaction on successful completion.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Close
{
}
