package com.arjuna.mwlabs.wscf.utils;

import java.lang.annotation.*;

/**
 * Annotation used to identify a ContextFactory and specify the protocol it supports and the type of high
 * level service it expects to be able to use it
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextProvider
{
    public String coordinationType();
    public String serviceType();
    public Class contextImplementation();
}
