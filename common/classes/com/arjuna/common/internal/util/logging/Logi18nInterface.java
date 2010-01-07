/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.common.internal.util.logging;

/**
 * Extension which adds i18n methods to LogInterface.
 * Note that methods which take a (String message) in the parent
 * interface are implicitly overridden to have the meaning (String key)
 * in this version, but don't actually appear in the interface here
 * as the signature is unchanged.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-01
 */
public interface Logi18nInterface extends LogInterface
{
    public void debug(String key, Object[] params);
    public void debug(String key, Object[] params, Throwable throwable);
    public void info(String key, Object[] params);
    public void info(String key, Object[] params, Throwable throwable);
    public void warn(String key, Object[] params);
    public void warn(String key, Object[] params, Throwable throwable);
    public void error(String key, Object[] params);
    public void error(String key, Object[] params, Throwable throwable);
    public void fatal(String key, Object[] params);
    public void fatal(String key, Object[] params, Throwable throwable);
}
