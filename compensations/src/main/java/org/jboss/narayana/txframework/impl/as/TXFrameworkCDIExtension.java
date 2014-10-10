/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.as;

import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBATxControlImpl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;


/**
 * @author paul.robinson@redhat.com, 2012-02-13
 */
public class TXFrameworkCDIExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        // Deprecated API
        bbd.addAnnotatedType(bm.createAnnotatedType(TXDataMap.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TXDataMapImpl.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(WSBATxControl.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(WSBATxControlImpl.class));
    }

}
