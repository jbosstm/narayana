/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.hp.mwtests.commonlogging.debugextension;

import com.arjuna.common.util.logging.*;

//import com.hp.mw.common.util.logging.CommonLevel;
//import com.hp.mw.common.util.logging.commonLogger;
//import com.hp.mw.common.util.logging.LoggerFactory;
//import com.hp.mw.common.util.logging.LogManager;
//import com.hp.mw.common.util.logging.Level;
//import com.hp.mw.common.util.logging.DebugLevel;


public class DebugExt
{
   static LogNoi18n myNoi18nLog = LogFactory.getLogNoi18n("com.hp.mwtests.commonlogging.debugextension.DebugExt");

   public static void main(String[] args)
   {

      myNoi18nLog.setLevels(DebugLevel.FULL_DEBUGGING, VisibilityLevel.VIS_ALL,
                      FacilityCode.FAC_ALL);

      myNoi18nLog.debug(DebugLevel.FUNCS_AND_OPS, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                        "This debug message is enabled since it matches default Finer Values");

      myNoi18nLog.debug(DebugLevel.CONSTRUCT_AND_DESTRUCT, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                        "This debug message is discarded since it does'nt match default Finer Values");

      myNoi18nLog.mergeDebugLevel(DebugLevel.FULL_DEBUGGING);
      myNoi18nLog.debug(DebugLevel.FULL_DEBUGGING, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                        "This debug message is enabled since it the Logger allows full debugging");
   }
}
