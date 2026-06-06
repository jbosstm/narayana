/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package com.arjuna.jta.distributed.example;

import javax.transaction.Synchronization;

/**
 * This is a simple Synchronization, any knowledge (such as the server name) it
 * has of the rest of the example is purely for debugging. It should be
 * considered a black box.
 */
public class TestSynchronization implements Synchronization {
	private String localServerName;

	public TestSynchronization(String localServerName) {
		this.localServerName = localServerName;
	}

	@Override
	public void beforeCompletion() {
		System.out.println(" TestSynchronization (" + localServerName + ")      beforeCompletion");
	}

	@Override
	public void afterCompletion(int status) {
		System.out.println(" TestSynchronization (" + localServerName + ")      afterCompletion");
	}
}
