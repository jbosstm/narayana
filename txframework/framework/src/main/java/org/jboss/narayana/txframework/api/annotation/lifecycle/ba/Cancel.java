package org.jboss.narayana.txframework.api.annotation.lifecycle.ba;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Method level annotation stating that this @ServiceInvocation method should Complete a ParticipantCompletion WS-BA
    Transaction on successful completion.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cancel
{
}
