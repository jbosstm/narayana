/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2020,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.jta.recovery.arjunacore;

/**
 * Some object used in recovery, such as Xids or XAResources, are more useful if they carry naming
 * information around with them. This is conceptually similar to XAResourceWrapper.
 */
interface NameScopedElement {

    /**
     * The name of source (usually a DataSource) of the associated element, or null for unknown/anonymous.
     */
    String getJndiName();

    /**
     * Is the jndiName both non-null and the same as the other's jndiName?
     * @param other
     * @return
     */
    boolean isSameName(NameScopedElement other);

    /**
     * Is the jndiName null?
     * @return
     */
    boolean isAnonymous();
}