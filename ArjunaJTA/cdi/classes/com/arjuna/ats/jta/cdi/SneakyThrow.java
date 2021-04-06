/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package com.arjuna.ats.jta.cdi;

/**
 * An utility class which makes possible to throw any exception as a {@link RuntimeException}.
 * It means to throw checked exception (subtype of Throwable or Exception) as un-checked exception.
 * This considers the Java 8 inference rule that states that a {@code throws E} is inferred as {@code RuntimeException}.
 */
public class SneakyThrow {
        private SneakyThrow() {
            throw new IllegalStateException("utility class, do not instance");
        }

        /**
         * This method can be used in {@code throw} statement
         * such as: {@code throw sneakyThrow(exception);}.
         */
        @SuppressWarnings("unchecked")
        public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
            throw (E) e;
        }
}
