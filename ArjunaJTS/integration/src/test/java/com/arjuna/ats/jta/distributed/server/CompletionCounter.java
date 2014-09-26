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
package com.arjuna.ats.jta.distributed.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CompletionCounter {

	private static CompletionCounter instance;

	private Map<String, Integer> commitCounter = new HashMap<String, Integer>();
	private Map<String, Integer> rollbackCounter = new HashMap<String, Integer>();

	public static CompletionCounter getInstance() {
		if (instance == null) {
			instance = new CompletionCounter();
		}
		return instance;
	}

	protected CompletionCounter() {

	}

	public void incrementCommit(String nodeName) {
		Integer integer = commitCounter.get(nodeName);
		if (integer == null) {
			integer = new Integer(1);
		} else {
			integer = new Integer(integer.intValue() + 1);
		}
		commitCounter.put(nodeName, integer);

	}

	public void incrementRollback(String nodeName) {
		Integer integer = rollbackCounter.get(nodeName);
		if (integer == null) {
			integer = new Integer(1);
		} else {
			integer = new Integer(integer.intValue() + 1);
		}
		rollbackCounter.put(nodeName, integer);
	}

	public int getCommitCount(String nodeName) {
		Integer integer = commitCounter.get(nodeName);
		if (integer == null) {
			integer = new Integer(0);
		}
		return integer;
	}

	public int getRollbackCount(String nodeName) {
		Integer integer = rollbackCounter.get(nodeName);
		if (integer == null) {
			integer = new Integer(0);
		}
		return integer;
	}

	public int getTotalCommitCount() {
		Integer toReturn = 0;
		Iterator<Integer> iterator = commitCounter.values().iterator();
		while (iterator.hasNext()) {
			toReturn += iterator.next();
		}
		return toReturn;
	}

	public int getTotalRollbackCount() {
		Integer toReturn = 0;
		Iterator<Integer> iterator = rollbackCounter.values().iterator();
		while (iterator.hasNext()) {
			toReturn += iterator.next();
		}
		return toReturn;
	}

	public void reset() {
		commitCounter.clear();
		rollbackCounter.clear();
	}
}
