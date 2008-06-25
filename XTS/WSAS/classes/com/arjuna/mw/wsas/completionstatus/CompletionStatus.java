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
 * $Id: CompletionStatus.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 */

package com.arjuna.mw.wsas.completionstatus;

/**
 * Implementations of this interface represent the various ways in which
 * an activity can terminate. Services are expected to extend this in
 * specific ways. Uniqueness is guaranteed by the package scope of the
 * final implementation.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CompletionStatus.java,v 1.1 2002/11/25 10:51:41 nmcl Exp $
 * @since 1.0.
 */

/*
 * There is an argument that the basic activity service layer should know
 * nothing about success of failure and hence does not need completion
 * statuses. We have them here for now until we decide to remove them!
 */

public interface CompletionStatus
{

    /**
     * Two statuses are equal if their targets are the same.
     */

    public boolean equals (Object param);
 
}
