/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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