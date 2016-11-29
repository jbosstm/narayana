/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.tomcat.jta;

import com.arjuna.ats.jta.common.jtaPropertyManager;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * Object factory to create instances of {@link javax.transaction.TransactionManager}.
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransactionManagerFactory implements ObjectFactory {

    /**
     * User internal factory method to instantiate new or reuse existing instance of
     * {@link javax.transaction.TransactionManager}.
     * 
     * @param obj
     * @param name
     * @param nameCtx
     * @param environment
     * @return instance of {@link javax.transaction.TransactionManager} or {@code null} if instantiation has failed.
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
    }

}
