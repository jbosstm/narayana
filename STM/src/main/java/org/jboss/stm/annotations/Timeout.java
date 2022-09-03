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
 * If pessimistic concurrency control is being used then a conflict will
 * immediately cause the operation to fail and the application can do something
 * else. If instead the developer wants the system to retry getting the lock
 * before returning, then this annotation defines the time between each retry attempt.
 * This is milliseconds.
 * 
 * @author marklittle
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Timeout
{
    int period ();
}
