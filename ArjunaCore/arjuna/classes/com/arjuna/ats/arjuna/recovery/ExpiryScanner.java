/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

/**
 * Interface for Expiry scanner plug-ins.
 * ExpiryScanners check for ancient entries in the ObjectStore (or elsewhere) to 
 * avoid leaving long-dead and irrelevant entries lying around for ever.
 * The scan() method of each registered ExpiryScanner implementation is called 
 * by the RecoveryManager at an interval expiryScanInterval (hours).
 */

public interface ExpiryScanner
{
   /**
    * perform a scan
    */
   public void scan() ;

   /**
    * Is this scanner to be used.  (E.g. if zero age means "don't remove", and it
    * has been set to zero, toBeUsed replies false)
    */
   public boolean toBeUsed() ;
}