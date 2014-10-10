/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.api;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate a bean is to be scoped to the current active
 * compensation-based transaction.
 * <p/>
 * Note: There is currently a bug that prevents this scope from working
 * in implementations of:
 * <p/>
 * org.jboss.narayana.compensations.api.CompensationHandler
 * org.jboss.narayana.compensations.api.ConfirmationHandler
 * org.jboss.narayana.compensations.api.TransactionLoggedHandler
 * <p/>
 * See here for more details: https://issues.jboss.org/browse/JBTM-1711
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@NormalScope(passivating = true)
public @interface CompensationScoped {

}