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

package org.jboss.narayana.compensations.internal.context;

import org.jboss.narayana.compensations.api.CompensationScoped;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.enterprise.context.ContextException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContextUnitTest {

    @Mock
    private CompensationContextStateManager compensationContextStateManager;

    @Mock
    private CompensationContextState compensationContextState;

    @Mock
    private Contextual<Object> contextual;

    @Mock
    private PassivationCapableContextual passivationCapableContextual;

    @Mock
    private CreationalContext<Object> creationalContext;

    @Mock
    private Object object;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetScope() {
        CompensationContext compensationContext = new CompensationContext(null);
        assertTrue(CompensationScoped.class.equals(compensationContext.getScope()));
    }

    @Test(expected = ContextException.class)
    public void getShouldThrowExceptionWithoutContextual() {
        CompensationContext compensationContext = new CompensationContext(null);
        compensationContext.get(null);
    }

    @Test
    public void getShouldReturnNullWithNonPassivationCapableContextual() {
        CompensationContext compensationContext = new CompensationContext(null);
        assertNull(compensationContext.get(contextual));
    }

    @Test
    public void shouldGetObjectFromTheContext() {
        when(passivationCapableContextual.getId()).thenReturn("id");
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        when(compensationContextState.getResource(eq("id"))).thenReturn(object);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        assertEquals(object, compensationContext.get(passivationCapableContextual));
        verify(compensationContextState, times(1)).getResource(eq("id"));
    }

    @Test
    public void shouldNotFindAnyObjectAndReturnNull() {
        when(passivationCapableContextual.getId()).thenReturn("id");
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        when(compensationContextState.getResource(eq("id"))).thenReturn(null);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        assertEquals(null, compensationContext.get(passivationCapableContextual));
        verify(compensationContextState, times(1)).getResource(eq("id"));
    }

    @Test
    public void shouldGetObjectFromCreationalContextAndAddToTheContext() {
        when(passivationCapableContextual.getId()).thenReturn("id");
        when(passivationCapableContextual.create(creationalContext)).thenReturn(object);
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        when(compensationContextState.getResource(eq("id"))).thenReturn(null);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        assertEquals(object, compensationContext.get(passivationCapableContextual, creationalContext));
        verify(compensationContextState, times(1)).getResource(eq("id"));
        verify(compensationContextState, times(1)).addResource(eq("id"), eq(object));
        verify(passivationCapableContextual, times(1)).create(creationalContext);
    }

    @Test
    public void contextShouldBeActive() {
        when(compensationContextStateManager.isActive()).thenReturn(true);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        assertTrue(compensationContext.isActive());
    }

    @Test
    public void contextShouldNotBeActive() {
        when(compensationContextStateManager.isActive()).thenReturn(false);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        assertFalse(compensationContext.isActive());
    }

    @Test
    public void shouldDestroyContext() {
        when(passivationCapableContextual.getId()).thenReturn("id");
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        compensationContext.destroy(passivationCapableContextual);
        verify(passivationCapableContextual, times(1)).getId();
        verify(compensationContextState, times(1)).removeResource("id");
    }

    @Test
    public void shouldNotDestroyContextWithoutPassivationCapableContextual() {
        when(compensationContextStateManager.getCurrent()).thenReturn(compensationContextState);
        CompensationContext compensationContext = new CompensationContext(compensationContextStateManager);
        compensationContext.destroy(contextual);
        verify(compensationContextState, times(0)).removeResource("id");
    }

    private interface PassivationCapableContextual extends Contextual<Object>, PassivationCapable {
    }
}
