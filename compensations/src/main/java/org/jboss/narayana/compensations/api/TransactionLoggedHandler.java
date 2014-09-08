/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.api;

/**
 * Provides a call-back handler, invoked after the transaction manager logs the confirmation and compensation handlers.
 *
 * The handler is registered via the org.jboss.narayana.compensations.api.TxLogged annotation.
 *
 * The 'success' flag indicates whether the logging was successful. 'Success' indicates that the application will later
 * be informed of the outcome of the transaction, via the confirmation and compensation handlers, even if the server crashes
 * and recovery is required.
 *
 * Critical logic that most be done after transaction logging can be placed in this handler.
 *
 * @author paul.robinson@redhat.com 21/03/2013
 */
public interface TransactionLoggedHandler {

    /**
     * Notification that the transaction has been logged
     *
     * @param success indicates whether the log was succesful or not.
     */
    public void transactionLogged(boolean success);
}
