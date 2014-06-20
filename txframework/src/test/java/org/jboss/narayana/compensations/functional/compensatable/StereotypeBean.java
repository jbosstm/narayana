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
package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.impl.BAControllerFactory;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@TestStereotype
public class StereotypeBean {

    public void doSomething() throws TestException {
        try {
            final Object txContext = BAControllerFactory.getInstance().getCurrentTransaction();

            if (txContext == null) {
                throw new RuntimeException("No transaction is active");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error looking up current transaction", e);
        }

        throw new TestException();
    }

}
