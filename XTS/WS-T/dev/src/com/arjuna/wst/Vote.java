/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * When asked to prepare, a 2PC participant returns one of three types of
 * vote:
 *
 * ReadOnly: does not need to be informed of the transaction outcome as no
 * state updates were made.
 * Prepared: it is prepared to commit or rollback depending on the final
 * transaction outcome, and it has made sufficient state updates persistent
 * to accomplish this.
 * Aborted: the participant has aborted and the transaction should also
 * attempt to do so.
 *
 * @see com.arjuna.wst.ReadOnly
 * @see com.arjuna.wst.Prepared
 * @see com.arjuna.wst.Aborted
 */

public interface Vote
{
}