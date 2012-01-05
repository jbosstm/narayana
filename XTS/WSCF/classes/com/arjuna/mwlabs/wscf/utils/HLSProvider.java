package com.arjuna.mwlabs.wscf.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * Annotation used to tag HLS providers and identify the type of service they implement
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HLSProvider
{
    String serviceType();
}
