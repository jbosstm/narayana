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
package com.arjuna.ats.internal.jta.utils;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the jtax module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface jtaxI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

	@Message(id = 24001, value = "XA recovery committing {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_recovery_jts_orbspecific_commit(String arg0);

	@Message(id = 24002, value = "XA recovery rolling back {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_recovery_jts_orbspecific_rollback(String arg0);

	@Message(id = 24003, value = "{0} caught exception during construction: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_consterror(String arg0, String arg1);

	@Message(id = 24004, value = "Caught the following error while trying to single phase complete resource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_coperror(String arg0);

	@Message(id = 24005, value = "Committing of resource state failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_createstate();

	@Message(id = 24006, value = "{0} caused an error from resource {1} in transaction {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_generror(String arg0, String arg1, String arg2);

	@Message(id = 24007, value = "You have chosen to disable the Multiple Last Resources warning. You will see it only once.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_lastResource_disableWarning();

	@Message(id = 24008, value = "Adding multiple last resources is disallowed. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_lastResource_disallow(String arg0);

	@Message(id = 24009, value = "Multiple last resources have been added to the current transaction. This is transactionally unsafe and should not be relied upon. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_lastResource_multipleWarning(String arg0);

	@Message(id = 24010, value = "You have chosen to enable multiple last resources in the transaction manager. This is transactionally unsafe and should not be relied upon.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_lastResource_startupWarning();

	@Message(id = 24011, value = "Reading state caught: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_loadstateread(String arg0);

	@Message(id = 24012, value = "Could not find new XAResource to use for recovering non-serializable XAResource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_norecoveryxa(String arg0);

	@Message(id = 24013, value = "{0} caught NotPrepared exception during recovery phase!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_notprepared(String arg0);

	@Message(id = 24014, value = "{0} - null or invalid transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_nulltransaction(String arg0);

	@Message(id = 24015, value = "XAResource prepare failed on resource {0} for transaction {1} with: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_preparefailed(String arg0, String arg1, String arg2);

	@Message(id = 24016, value = "Recovery of resource failed when trying to call {0} got: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_recfailed(String arg0, String arg1);

	@Message(id = 24017, value = "Attempted shutdown of resource failed with:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_remconn();

	@Message(id = 24018, value = "Exception on attempting to resource XAResource: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_restoreerror1(String arg0);

	@Message(id = 24019, value = "Unexpected exception on attempting to resource XAResource: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_restoreerror2(String arg0);

	@Message(id = 24020, value = "Could not serialize a serializable XAResource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_saveState();

	@Message(id = 24021, value = "{0} caught unexpected exception: {1} during recovery phase!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_unexpected(String arg0, String arg1);

	@Message(id = 24022, value = "Updating of resource state failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_updatestate();

	@Message(id = 24023, value = "{0} caused an XA error: {1} from resource {2} in transaction {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_resources_jts_orbspecific_xaerror(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 24024, value = "thread is already associated with a transaction and subtransaction support is not enabled!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_alreadyassociated();

	@Message(id = 24025, value = "Delist of resource failed with: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_delistfailed(String arg0);

	@Message(id = 24026, value = "Ending suspended RMs failed when rolling back the transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_endsuspendfailed1();

	@Message(id = 24027, value = "Ending suspended RMs failed when rolling back the transaction, but transaction rolled back.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_endsuspendfailed2();

	@Message(id = 24028, value = "illegal resource state:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_illegalstate();

	@Message(id = 24029, value = "Transaction is not active.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_inactivetx();

	@Message(id = 24030, value = "invalid transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_invalidtx();

	@Message(id = 24031, value = "Invalid transaction.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_invalidtx2();

	@Message(id = 24032, value = "Work already active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_jca_busy();

	@Message(id = 24033, value = "failed to load Last Resource Optimisation Interface", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_lastResourceOptimisationInterface();

	@Message(id = 24034, value = "Could not enlist resource because the transaction is marked for rollback.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_markedrollback();

	@Message(id = 24035, value = "No such transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_nosuchtx();

	@Message(id = 24036, value = "Current transaction is not a TransactionImple", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_nottximple();

	@Message(id = 24037, value = "no transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_notx();

	@Message(id = 24038, value = "no transaction! Caught:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_notxe();

	@Message(id = 24039, value = "No such transaction.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_nox();

	@Message(id = 24040, value = "paramater is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_nullparam();

	@Message(id = 24041, value = "{0} could not register transaction: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_regerror(String arg0, String arg1);

	@Message(id = 24042, value = "is already suspended!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_ressusp();

	@Message(id = 24043, value = "An error occurred while checking if this is a new resource manager:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_rmerror();

	@Message(id = 24044, value = "{0} could not mark the transaction as rollback only: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_rollbackerror(String arg0, String arg1);

	@Message(id = 24045, value = "setRollbackOnly called from:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_setrollback();

	@Message(id = 24046, value = "{0} returned XA error {1} for transaction {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_starterror(String arg0, String arg1, String arg2);

	@Message(id = 24047, value = "Not allowed to terminate subordinate transaction directly.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_subordinate_invalidstate();

	@Message(id = 24048, value = "Synchronizations are not allowed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_syncerror();

	@Message(id = 24049, value = "cleanup synchronization failed to register:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_syncproblem();

	@Message(id = 24050, value = "The transaction implementation threw a RollbackException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_syncrollbackexception();

	@Message(id = 24051, value = "The transaction implementation threw a SystemException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_systemexception();

	@Message(id = 24052, value = "Active thread error:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_threaderror();

	@Message(id = 24053, value = "{0} attempt to delist unknown resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_unknownres(String arg0);

	@Message(id = 24054, value = "The current transaction does not match this transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_wrongstatetx();

	@Message(id = 24055, value = "Could not call end on a suspended resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_xaenderror();

	@Message(id = 24056, value = "{0} caught XA exception: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_com_arjuna_ats_internal_jta_transaction_jts_xaerror(String arg0, String arg1);

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
     */
}
