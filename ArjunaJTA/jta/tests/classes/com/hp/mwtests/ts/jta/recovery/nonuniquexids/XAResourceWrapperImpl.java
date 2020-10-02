/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
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
package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import org.jboss.tm.XAResourceWrapper;

import javax.transaction.xa.XAResource;

// unit test helper based on a Jonathan Halliday's code
public class XAResourceWrapperImpl extends XAResourceImpl implements XAResourceWrapper {

    public XAResourceWrapperImpl(ResourceManager resourceManager, String name) {
        super(resourceManager, name);
    }

    @Override
    public XAResource getResource() {
        return this;
    }

    @Override
    public String getProductName() {
        return null;
    }

    @Override
    public String getProductVersion() {
        return null;
    }

    @Override
    public String getJndiName() {
        return resourceManager.getName();
    }
}

