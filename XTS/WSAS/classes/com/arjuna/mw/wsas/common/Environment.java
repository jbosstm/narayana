/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java,v 1.5 2004/03/15 13:24:59 nmcl Exp $
 */

package com.arjuna.mw.wsas.common;

/**
 */

public class Environment
{

    public static final String ACTIVITY_TIMEOUT = "com.arjuna.mw.wsas.activityTimeout";
    public static final String REAPER_MODE = "com.arjuna.mw.wsas.reaperMode";
    public static final String REAPER_TIMEOUT = "com.arjuna.mw.wsas.reaperTimeout";

    // to remove

    public static final String DEPLOYMENT_CONTEXT = "com.arjuna.mw.wsas.deploymentContext";
    
    public static final String REPLAY_TIMEOUT = "com.arjuna.mw.wsas.replayTimeout" ;
    
    public static final String REPLAY_COUNT = "com.arjuna.mw.wsas.replayCount" ;
}
