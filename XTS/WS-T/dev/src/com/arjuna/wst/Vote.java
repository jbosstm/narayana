/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Vote.java,v 1.2 2003/03/24 11:01:42 nmcl Exp $
 */

package com.arjuna.wst;

/**
 * When asked to prepare, a 2PC participant returns one of three types of
 * vote:
 *
 * ReadOnly: does not need to be informed of the transaction outcome as no
 * state updates were made.
 * Prepared: it is prepared to commit or rollback depending on the final
 * transaction outcome, and it has made sufficient state updates persistent
 * to accomplish this.
 * Aborted: the participant has aborted and the transaction should also
 * attempt to do so.
 *
 * @see com.arjuna.wst.ReadOnly
 * @see com.arjuna.wst.Prepared
 * @see com.arjuna.wst.Aborted
 */

public interface Vote
{
}
