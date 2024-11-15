/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.tools.osb.mbean;

/**
 * Enumeration of the commit status of a participant in an action/transaction
 */
public enum ParticipantStatus {PREPARED, PENDING, FAILED, READONLY, HEURISTIC}
