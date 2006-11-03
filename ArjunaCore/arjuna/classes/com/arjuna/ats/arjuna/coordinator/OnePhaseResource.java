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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: OnePhaseResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import java.io.PrintWriter;

import java.io.IOException;

/*
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OnePhaseResource.java 2342 2006-03-30 13:06:17Z  $
 * @since 3.2.
 */

public interface OnePhaseResource
{

    /**
     * Return values from TwoPhaseOutcome to indicate success or failure.
     *
     * If this fails, then we will automatically attempt to rollback any
     * other participants.
     */

    public int commit ();

    /**
     * Return values from TwoPhaseOutcome to indicate success or failure.
     */
    
    public int rollback ();

    public void pack (OutputObjectState os) throws IOException;
    
    public void unpack (InputObjectState os) throws IOException;
    
}

