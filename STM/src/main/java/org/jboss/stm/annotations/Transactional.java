/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * (C) 2005-2006,
 * @author mark.little@jboss.com
 */

package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Defines that implementations of the interface are to be managed within a transactional
 * container. Unless specified using other annotations, all public
 * methods will be assumed to modify the state of the object, i.e.,
 * require write locks. All state variables will be saved/restored
 * by default unless it is marked using the @State annotation or 
 * SaveState/RestoreState.
 * 
 * This assumes currently that all state modification and locking occurs through
 * public methods. This means that even if there are private, protected or
 * package scope methods that would change the state, they will not be tracked.
 * Therefore, the implementation class should not modify state unless by calling
 * its own public methods.
 * 
 * All methods should either be invoked within a transactional context or have
 * the Nested annotation applied, wherein the system will automatically create a
 * new transaction when the method is invoked.
 * 
 * NOTE this annotation needs to be provided on the interface so that the container
 * knows to create proxy objects for the derived types once it sees them.
 * 
 * @author marklittle
 *
 */

/*
 * TODO fix this, so that we can either proxy private, protected and package
 * methods, or provide a means whereby the implementation class can inform the
 * transaction system. For instance, maybe the implementation class can call into
 * the container that maintains the proxy, passing itself as a reference, and
 * drive setlock, modified etc. implicitly.
 */

/*
 * TODO check that the restriction on public method access is still true!
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Transactional
{
}