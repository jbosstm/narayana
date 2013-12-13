package com.arjuna.ats.arjuna.coordinator;

/**
 * In most cases resources defer exceptions and return an arjuna specific error code. If such a resource implements this interface 
 * then a resource user can get the last exception that occurred on the resource by calling getDeferredThrowable.  
 * @author sebplorenz
 */
public interface ExceptionDeferrer
{

   Throwable getDeferredThrowable();

}