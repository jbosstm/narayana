/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.ats.internal.jbossatx.logging;

import com.arjuna.common.internal.util.logging.LogFactoryInterface;
import com.arjuna.common.internal.util.logging.LogInterface;
import com.arjuna.common.internal.util.logging.Logi18nInterface;
import com.arjuna.common.util.exceptions.LogConfigurationException;

/**
 * CLF log factory for integration with JBoss log manager.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-11
 */
public class JBossLogFactory implements LogFactoryInterface
{
    /**
     * Method to return a named logger.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *             returned (the meaning of this name is only known to the underlying
     *             logging implementation that is being wrapped)
     * @throws com.arjuna.common.util.exceptions.LogConfigurationException
     *          if a suitable <code>LogInterface</code>
     *          instance cannot be returned
     */
    @Override
    public LogInterface getLog(String name) throws LogConfigurationException
    {
        return new JBossLogger(name);
    }

    /**
     * Determine if the underlying logging framework supports i18n or not.
     *
     * @return true if i18n is supported, false otherwise.
     */
    @Override
    public boolean isInternationalizationSupported()
    {
        return true;
    }

    /**
     * Method to return a named logger with a given ResourceBundle associated.
     *
     * @param name               Logical name of the <code>Log</code> instance to be
     *                           returned (the meaning of this name is only known to the underlying
     *                           logging implementation that is being wrapped)
     * @param resourceBundleName The name of the ResourceBundle to associate with the logger
     * @return an appropriately configured LogInterface implementation
     * @throws com.arjuna.common.util.exceptions.LogConfigurationException
     *          if a suitable <code>LogInterface</code>
     *          instance cannot be returned, including where the logger does not support i18n.
     */
    @Override
    public Logi18nInterface getLog(String name, String resourceBundleName) throws LogConfigurationException
    {
        return new JBossLogger(name, resourceBundleName);
    }
}
