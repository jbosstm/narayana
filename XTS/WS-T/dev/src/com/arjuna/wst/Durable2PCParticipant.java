/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * The Durable2PCParticipant. Unlike all traditional TP implementations, the
 * one-phase commit optimisation is not supported. It was in the 1.0
 * version of the protocol, but not now!
 */

public interface Durable2PCParticipant extends Participant
{
}