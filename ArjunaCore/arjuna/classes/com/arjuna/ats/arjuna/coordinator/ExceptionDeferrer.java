package com.arjuna.ats.arjuna.coordinator;

import java.util.List;

/**
 * In most cases resources defer exceptions and return an arjuna specific error code. If such a resource implements this interface 
 * then a resource user can get the last exception that occurred on the resource by calling getDeferredThrowable.
 * 
 * @author sebplorenz
 */
public interface ExceptionDeferrer
{

   /**
    * Adds all supressed throwables of this ExceptionDeferrer to the given list in order of appearance.
    * @param list 
    */
   void getDeferredThrowables(List<Throwable> list);

}
