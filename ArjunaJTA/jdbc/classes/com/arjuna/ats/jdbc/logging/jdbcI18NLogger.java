/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.jdbc.logging;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the jdbc module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface jdbcI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 17001, value = "Rollback not allowed by transaction service.", format = MESSAGE_FORMAT)
	public String get_aborterror();

	@Message(id = 17002, value = "Connection is already associated with a different transaction! Obtain a new connection for this transaction.", format = MESSAGE_FORMAT)
	public String get_alreadyassociated();

	@Message(id = 17003, value = "Checking transaction and found that this connection is already associated with a different transaction! Obtain a new connection for this transaction.", format = MESSAGE_FORMAT)
	public String get_alreadyassociatedcheck();

	@Message(id = 17004, value = "AutoCommit is not allowed by the transaction service.", format = MESSAGE_FORMAT)
	public String get_autocommit();

	@Message(id = 17005, value = "An error occurred during close:", format = MESSAGE_FORMAT)
	public String get_closeerror();

	@Message(id = 17006, value = "Invalid transaction during close {0}", format = MESSAGE_FORMAT)
	public String get_closeerrorinvalidtx(String arg0);

	@Message(id = 17007, value = "Connection will be closed now. Indications are that this db does not allow multiple connections in the same transaction {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_closingconnection(String arg0);

	@Message(id = 17008, value = "No modifier information found for db. Connection will be closed immediately {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_closingconnectionnull(String arg0);

	@Message(id = 17009, value = "Commit not allowed by transaction service.", format = MESSAGE_FORMAT)
	public String get_commiterror();

	@Message(id = 17010, value = "JDBC2 connection initialisation problem", format = MESSAGE_FORMAT)
	public String get_conniniterror();

	@Message(id = 17011, value = "Delist of resource failed.", format = MESSAGE_FORMAT)
	public String get_delisterror();

//	@Message(id = 17012, value = "Caught exception", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_drcclose();

	@Message(id = 17013, value = "Caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_drcdest(Throwable arg0);

//	@Message(id = 17014, value = "caught exception:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_drivers_exception();

//	@Message(id = 17015, value = "database not for", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_drivers_invaliddb();

	@Message(id = 17016, value = "No dynamic class specified!", format = MESSAGE_FORMAT)
	public String get_dynamicerror();

	@Message(id = 17017, value = "enlist of resource failed", format = MESSAGE_FORMAT)
	public String get_enlistfailed();

	@Message(id = 17018, value = "Failed to get modifier for driver:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_getmoderror(Throwable arg0);

//	@Message(id = 17019, value = "Caught exception", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_idrcclose();

	@Message(id = 17020, value = "Transaction is not active on the thread!", format = MESSAGE_FORMAT)
	public String get_inactivetransaction();

	@Message(id = 17021, value = "Could not get transaction information.", format = MESSAGE_FORMAT)
	public String get_infoerror();

//	@Message(id = 17022, value = "Caught exception", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_ircdest();

//	@Message(id = 17023, value = "{0} - failed to set isolation level: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_isolationlevelfailget(String arg0, String arg1);

	@Message(id = 17024, value = "{0} - failed to set isolation level", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_isolationlevelfailset(String arg0, Throwable arg1);

	@Message(id = 17025, value = "Could not resolve JNDI XADataSource", format = MESSAGE_FORMAT)
	public String get_jndierror();

	@Message(id = 17026, value = "Can't load ConnectionImple class {0}", format = MESSAGE_FORMAT)
	public String get_nojdbcimple(String arg0);

	@Message(id = 17027, value = "An exception occurred during initialisation.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_basic_initexp(Throwable arg0);

	@Message(id = 17028, value = "{0} could not find information for connection!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_basic_xarec(String arg0);

	@Message(id = 17029, value = "An exception occurred during initialisation.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_xa_initexp(Throwable arg0);

//	@Message(id = 17030, value = "{0} could not find information for connection!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_xa_xarec(String arg0);

	@Message(id = 17031, value = "rollback(Savepoint) not allowed inside distributed tx.", format = MESSAGE_FORMAT)
	public String get_releasesavepointerror();

	@Message(id = 17032, value = "{0} - could not mark transaction rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_rollbackerror(String arg0);

	@Message(id = 17033, value = "rollback(Savepoint) not allowed inside distributed tx.", format = MESSAGE_FORMAT)
	public String get_rollbacksavepointerror();

	@Message(id = 17034, value = "Cannot set readonly when within a transaction!", format = MESSAGE_FORMAT)
	public String get_setreadonly();

	@Message(id = 17035, value = "setSavepoint not allowed inside distributed tx.", format = MESSAGE_FORMAT)
	public String get_setsavepointerror();

//	@Message(id = 17036, value = "State must be:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_stateerror();

	@Message(id = 17037, value = "Could not resolve JNDI XADataSource", format = MESSAGE_FORMAT)
	public String get_xa_recjndierror();

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
    */
}
