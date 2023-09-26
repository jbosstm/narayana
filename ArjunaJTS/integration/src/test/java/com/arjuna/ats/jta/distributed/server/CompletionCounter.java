/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
			integer = 1;
		} else {
			integer = integer + 1;
		}
		commitCounter.put(nodeName, integer);

	}

	public void incrementRollback(String nodeName) {
		Integer integer = rollbackCounter.get(nodeName);
		if (integer == null) {
			integer = 1;
		} else {
			integer = integer + 1;
		}
		rollbackCounter.put(nodeName, integer);
		synchronized (this) {
			notify();
		}
	}

	public int getCommitCount(String nodeName) {
		Integer integer = commitCounter.get(nodeName);
		if (integer == null) {
			integer = 0;
		}
		return integer;
	}

	public int getRollbackCount(String nodeName) {
		Integer integer = rollbackCounter.get(nodeName);
		if (integer == null) {
			integer = 0;
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