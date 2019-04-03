/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013-2019, Red Hat, Inc., and individual contributors
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

import java.util.Objects;

import java.util.function.Supplier;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

/**
 * A class whose {@link #get(String, Supplier)} method is intended to
 * supply objects related to the JTA specification.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #get(String, Supplier)
 */
final class JTASupplier {

  /**
   * Creates a new {@link JTASupplier}.
   */
  private JTASupplier() {
    super();
  }

  /**
   * Attempts to look up an object in JNDI using the supplied {@code
   * jndiName}, and, if that fails or if JNDI is not available,
   * returns the result of invoking the supplied {@code fallback}'s
   * {@link Supplier#get() get()} method.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param <T> the type of the object to be returned
   *
   * @param jndiName the name to look up; must not be {@code null}
   *
   * @param fallback the {@link Supplier} to use to acquire the object
   * in question if JNDI is not available; may be {@code null} in
   * which case {@code null} may very well be returned
   *
   * @return the object in question, or {@code null}
   *
   * @exception NullPointerException if {@code jndiName} is {@code
   * null}
   *
   * @exception NamingException if JNDI lookup fails, but <em>not</em>
   * if an {@link InitialContext} could not be acquired; the {@link
   * NamingException} thrown is guaranteed not to be an instance of
   * {@link NoInitialContextException}
   */
  static final <T> T get(final String jndiName,
                         final Supplier<? extends T> fallback)
    throws NamingException {
    Objects.requireNonNull(jndiName);

    final Context initialContext;
    Context tempContext = null;
    try {
      tempContext = new InitialContext();
    } catch (final NoInitialContextException noInitialContextException) {
      // Possible in certain combinations of JNDI implementations and
      // CDI SE situations.
    } finally {
      initialContext = tempContext;
      tempContext = null;
    }

    T returnValue = null;

    if (initialContext != null) {
      NamingException e = null;
      try {
        @SuppressWarnings("unchecked")
        final T temp = (T)initialContext.lookup(jndiName);
        returnValue = temp;
      } catch (final NoInitialContextException noInitialContextException) {
        // Possible in certain combinations of JNDI implementations and
        // CDI SE situations.
      } catch (final NamingException namingException) {
        e = namingException;
        throw namingException;
      } finally {
        try {
          initialContext.close();
        } catch (final NamingException namingException) {
          if (e != null) {
            e.addSuppressed(namingException);
          } else {
            e = namingException;
          }
          throw e;
        }
      }

    }

    if (returnValue == null && fallback != null) {
      returnValue = fallback.get();
    }

    return returnValue;
  }

}
