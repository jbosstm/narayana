/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.internal.util.logging.basic;

import com.arjuna.common.util.exceptions.LogConfigurationException;
import com.arjuna.common.internal.util.logging.LogFactoryInterface;
import com.arjuna.common.internal.util.logging.LogInterface;

/**
 * LogFactoryInterface impl for the built-in logger.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class BasicLogFactory implements LogFactoryInterface
{
    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *             returned (the meaning of this name is only known to the underlying
     *             logging implementation that is being wrapped)
     * @throws com.arjuna.common.util.exceptions.LogConfigurationException
     *          if a suitable <code>Log</code>
     *          instance cannot be returned
     */
    @Override
    public LogInterface getLog(String name) throws LogConfigurationException
    {
        return new BasicLog(name);
    }
}
