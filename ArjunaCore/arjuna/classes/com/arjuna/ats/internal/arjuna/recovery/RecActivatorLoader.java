/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.arjuna.ats.internal.arjuna.recovery ;

import java.util.*;

import com.arjuna.ats.arjuna.recovery.RecoveryActivator ;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * RecoveryActivators are dynamically loaded. The recoveryActivator to load
 * are specified by properties beginning with "recoveryActivator"
 * <P>
 * @author Malik Saheb
 * @since ArjunaTS 3.0
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_1 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_1] - Attempt to load recovery activator with null class name!
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_2 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_2] - Recovery module {0} does not conform to RecoveryActivator interface
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_3 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_3] - Loading recovery activator: {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_4 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_4] - Loading recovery activator: {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_5 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_5] - Loading recovery module: could not find class {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_6 [com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_6] - Start RecoveryActivators
*/

public class RecActivatorLoader
{

   public RecActivatorLoader()
   {
       initialise();

       // Load the Recovery Activators
       loadRecoveryActivators();

       startRecoveryActivators();

   }

  /**
   * Start the RecoveryActivator
   */

  public void startRecoveryActivators()
      //public void run()
  {
      if (tsLogger.arjLoggerI18N.isInfoEnabled())
	  {
	      tsLogger.arjLoggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_6");
	  }

      Enumeration activators = _recoveryActivators.elements();

      while (activators.hasMoreElements())
	  {
	      RecoveryActivator acti = (RecoveryActivator) activators.nextElement();
	      acti.startRCservice();
	  }

      return;

  }

    // These are loaded in list iteration order.
    private static void loadRecoveryActivators ()
    {
        Vector<String> activatorNames = new Vector<String>(recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryActivators());

        for(String activatorName : activatorNames) {
            loadActivator(activatorName);
        }
    }

  private static void loadActivator (String className)
  {
      if (tsLogger.arjLogger.isDebugEnabled()) {
          tsLogger.arjLogger.debug("Loading recovery activator " +
                  className);
      }

      if (className == null)
	  {
	      if (tsLogger.arjLoggerI18N.isWarnEnabled())
		  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_1");

	      return;
	  }
      else
	  {
	      try
		  {
		      Class c = Thread.currentThread().getContextClassLoader().loadClass( className ) ;

		      try
			  {
			      RecoveryActivator ra = (RecoveryActivator) c.newInstance() ;
			      _recoveryActivators.add( ra );
			  }
		      catch (ClassCastException e)
			  {
			      if (tsLogger.arjLoggerI18N.isWarnEnabled())
				  {
				      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLooader_2",
								  new Object[]{className});
				  }
			  }
		      catch (IllegalAccessException iae)
			  {
			      if (tsLogger.arjLoggerI18N.isWarnEnabled())
				  {
				      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_3",
								  new Object[]{iae});
				  }
			    }
		      catch (InstantiationException ie)
            {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_4",
						    new Object[]{ie});
		    }
            }

		      c = null;
		  }
	      catch ( ClassNotFoundException cnfe )
		  {
		      if (tsLogger.arjLoggerI18N.isWarnEnabled())
			  {
			      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.RecActivatorLoader_5",
							  new Object[]{className});
			  }
		  }
      }
  }

    private final void initialise ()
   {
       _recoveryActivators = new Vector();
   }

    // this refers to the recovery activators specified in the recovery manager
    // property file which are dynamically loaded.
    private static Vector _recoveryActivators = null ;

}









