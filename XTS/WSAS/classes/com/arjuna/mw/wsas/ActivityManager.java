/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActivityManager.java,v 1.1 2002/11/25 10:51:40 nmcl Exp $
 */

package com.arjuna.mw.wsas;

import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidHLSException;

/**
 * The activity manager is the way in which an HLS can register
 * itself with the activity service. This allows it to be informed
 * of the lifecycle of activities and to augment that lifecyle and
 * associated context.
 *
 * An HLS can be associated with all threads (globally) or with only
 * a specific thread (locally).
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActivityManager.java,v 1.1 2002/11/25 10:51:40 nmcl Exp $
 * @since 1.0.
 */

public interface ActivityManager
{

    /**
     * Register the specified HLS with the activity service.
     *
     * @param service The HLS to register.
     *
     * @exception InvalidHLSException Thrown if the HLS is invalid in the
     * current execution environment.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void addHLS (HLS service) throws InvalidHLSException, SystemException;

    /**
     * Unregister the specified HLS with the activity service.
     *
     * @param service The HLS to unregister.
     *
     * @exception InvalidHLSException Thrown if the HLS is invalid in the
     * current execution environment.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void removeHLS (HLS service) throws InvalidHLSException, SystemException;

    /**
     * Allows an invoker to obtain a list of all registered HLSs.
     * Elements at the start of the array have higher priority than those at
     * the end.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the list of HLSs.
     *
     */

    public HLS[] allHighLevelServices () throws SystemException;

    /**
     * Allows an invoker to obtain a specific registered HLS supporting a given coordination type.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the HLS.
     *
     */

    public HLS getHighLevelService (String serviceType) throws SystemException;
}
