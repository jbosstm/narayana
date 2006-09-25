/*
*
 * Copyright (C) 2005
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.ats.jta.resources;

import javax.transaction.xa.XAResource;

/**
 * Marker interface for Last Resource Commit Optimisation.
 * 
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 * @since ATS 4.1
 */
public interface LastResourceCommitOptimisation extends XAResource
{
}
