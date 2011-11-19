package org.jboss.jbossts.txframework.api.annotation.management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field level annotation used to enable injection of a data control into service participant
 * and lifecycle management classes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataManagement
{
}