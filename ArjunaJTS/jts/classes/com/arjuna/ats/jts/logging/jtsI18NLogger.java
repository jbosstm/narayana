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
package com.arjuna.ats.jts.logging;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the jts module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface jtsI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

//    @Message(id = 22001, value = "ExpiredAssumedCompleteScanner created, with expiry time of {0}  seconds", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_arjuna_recovery_ExpiredAssumedCompleteScanner_1(String arg0);

//	@Message(id = 22002, value = "ExpiredAssumedCompleteScanner - scanning to remove items from before {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_arjuna_recovery_ExpiredAssumedCompleteScanner_2(String arg0);

	@Message(id = 22003, value = "Removing old assumed complete transaction {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_arjuna_recovery_ExpiredAssumedCompleteScanner_3(Uid arg0);

//	@Message(id = 22004, value = "Expiry scan interval set to {0} seconds", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_arjuna_recovery_ExpiredAssumedCompleteScanner_4(String arg0);

//	@Message(id = 22005, value = "{0}  has inappropriate value ({1})", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_arjuna_recovery_ExpiredAssumedCompleteScanner_5(String arg0, String arg1);

	@Message(id = 22006, value = "The ORB has not been initialized yet", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_ORBManager();

//	@Message(id = 22007, value = "{0} expected a Coordinator reference and did not get one: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_context_coorref(String arg0, String arg1);

	@Message(id = 22008, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_context_genfail(String arg0, @Cause() Throwable arg1);

	@Message(id = 22009, value = "{0} does not support ORB: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_context_orbnotsupported(String arg0, String arg1);

	@Message(id = 22010, value = "Failed when getting a reference to PICurrent.", format = MESSAGE_FORMAT)
	public String get_context_picreffail();

	@Message(id = 22011, value = "Failed to cancel transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_cwabort(@Cause() Throwable arg0);

	@Message(id = 22012, value = "Failed to mark transaction as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_cwcommit(@Cause() Throwable arg0);

	@Message(id = 22013, value = "Failed to cancel transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_cwabort(@Cause() Throwable arg0);

	@Message(id = 22014, value = "{0} - default already set!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_fldefault(String arg0);

	@Message(id = 22015, value = "{0} - could not find {1} to remove.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_ipfail(String arg0, Uid arg1);

	@Message(id = 22016, value = "Nested transactions not identical.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_ipnt();

	@Message(id = 22017, value = "Interposed hierarchy is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_ipnull();

	@Message(id = 22018, value = "TopLevel transactions not identical: {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_iptl(Uid arg0, Uid arg1);

	@Message(id = 22019, value = "{0} - error, no child found!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_nochild(String arg0);

	@Message(id = 22020, value = "{0} - not my child!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_notchild(String arg0);

	@Message(id = 22021, value = "hierarchy: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_resources_arjuna_problemhierarchy(String arg0);

	@Message(id = 22022, value = "{0} for transaction {1} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_sfcaught(String arg0, Uid arg1, @Cause() Throwable arg2);

	@Message(id = 22023, value = "{0} - no parent transaction given!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_interposition_sfnoparent(String arg0);

	@Message(id = 22024, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_generror(String arg0, @Cause() Throwable arg1);

	@Message(id = 22025, value = "{0} attempt to mark transaction {1} as rollback only threw exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_rbofail(String arg0, Uid arg1, @Cause() Throwable arg2);

	@Message(id = 22026, value = "Creation of RecoveryCoordinator for {0} threw exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_rccreate(Uid arg0, @Cause() Throwable arg1);

	@Message(id = 22027, value = "not created!", format = MESSAGE_FORMAT)
	public String get_orbspecific_coordinator_rcnotcreated();

	@Message(id = 22028, value = "{0} called on still running transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_txrun(String arg0);

	@Message(id = 22029, value = "{0} - could not get unique identifier of object.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_uidfail(String arg0);

	@Message(id = 22030, value = "{0} - none zero Synchronization list!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_coordinator_zsync(String arg0);

	@Message(id = 22031, value = "could not destroy object:", format = MESSAGE_FORMAT)
	public String get_orbspecific_destroyfailed();

	@Message(id = 22032, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_coordinator_generror(String arg0, @Cause() Throwable arg1);

	@Message(id = 22033, value = "{0} - synchronizations have not been called!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_coordinator_syncerror(String arg0);

	@Message(id = 22034, value = "{0} - transaction not in prepared state: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_coordinator_txnotprepared(String arg0, String arg1);

	@Message(id = 22035, value = "{0} could not destroy object", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_destfailed(String arg0, @Cause() Throwable arg1);

	@Message(id = 22036, value = "Could not remove child {0} from {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_childerror(Uid arg0, Uid arg1);

	@Message(id = 22037, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_generror(String arg0, @Cause() Throwable arg1);

	@Message(id = 22038, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_generror_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 22039, value = "{0} - could not register interposed hierarchy!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_ipfailed(String arg0);

	@Message(id = 22040, value = "{0} - could not register interposed hierarchy!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_ipfailed_2(String arg0);

	@Message(id = 22041, value = "{0} - no coordinator to use!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_nocoord(String arg0);

	@Message(id = 22042, value = "{0} - no transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_notx(String arg0);

	@Message(id = 22043, value = "{0} - attempt to commit with null control!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_nullcontrol_1(String arg0);

	@Message(id = 22044, value = "{0} - attempt to rollback transaction will null control!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_nullcontrol_2(String arg0);

	@Message(id = 22045, value = "{0} - could not register as no Coordinator has been given!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_arjuna_nullcoord(String arg0);

	@Message(id = 22046, value = "Failed to destroy server-side synchronization object!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_destroyfailed();

	@Message(id = 22047, value = "Concurrent children found for restricted interposition!", format = MESSAGE_FORMAT)
	public String get_orbspecific_interposition_resources_restricted_contx_1();

	@Message(id = 22048, value = "{0} Concurrent children found for restricted interposition!", format = MESSAGE_FORMAT)
	public String get_orbspecific_interposition_resources_restricted_contx_4(String arg0);

	@Message(id = 22049, value = "{0} - found concurrent ({1}) transactions!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_restricted_contxfound_1(String arg0, String arg1);

	@Message(id = 22050, value = "{0} - found concurrent ({1}) transactions!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_restricted_contxfound_3(String arg0, String arg1);

	@Message(id = 22051, value = "{0} status of transaction is different from our status: <{1}, {2}>", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_stateerror(String arg0, String arg1, String arg2);

	@Message(id = 22052, value = "{0} - could not register interposed hierarchy!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_strict_ipfailed(String arg0);

	@Message(id = 22053, value = "{0} - could not register interposed hierarchy!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_interposition_resources_strict_iptlfailed(String arg0);

	@Message(id = 22054, value = "Invalid Uid:", format = MESSAGE_FORMAT)
	public String get_orbspecific_invaliduid();

	@Message(id = 22055, value = "Cannot create a codec of the required encoding.", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_context_codeccreate();

	@Message(id = 22056, value = "{0} - a failure occured when getting {1} codec - unknown encoding.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_context_codecerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22057, value = "{0} - duplicate interceptor name for {1} when registering", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_context_duplicatename(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22058, value = "Context interceptor caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_context_error(@Cause() Throwable arg0);

	@Message(id = 22059, value = "Invalid portable interceptor transaction parameter!", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_context_invalidparam();

	@Message(id = 22060, value = "A server-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_context_sie();

	@Message(id = 22061, value = "{0} caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_context_srie(String arg0, @Cause() Throwable arg1);

	@Message(id = 22062, value = "Cannot create a codec of the required encoding.", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_interposition_codeccreate();

	@Message(id = 22063, value = "{0} - a failure occured when getting {1} codec - unknown encoding.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_interposition_codecerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22064, value = "{0} - duplicate interceptor name for {1} when registering", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_interposition_duplicatename(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22065, value = "Invalid portable interceptor transaction parameter!", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_interposition_invalidparam();

	@Message(id = 22066, value = "A server-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_interposition_sie();

	@Message(id = 22067, value = "{0} caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_interceptors_interposition_srie(String arg0, @Cause() Throwable arg1);

//	@Message(id = 22068, value = "Client Interceptor for RecoveryCoordinators created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_ClientForwardInterceptor_1();

	@Message(id = 22069, value = "Failed to retreive the Object reference of the default RecoverCoordinator Object.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_ClientForwardInterceptor_2(@Cause() Throwable arg0);

//	@Message(id = 22070, value = "Failed to obtain the ObjectId string of the RecveryCoordinator target.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_ClientForwardInterceptor_3();

	@Message(id = 22071, value = "Failed to build service context with the ObjectId", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_ClientForwardInterceptor_4(@Cause() Throwable arg0);

	@Message(id = 22072, value = "Failed in ClientInitializer::post_init -", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_ClientInitializer_1(@Cause() Throwable arg0);

//	@Message(id = 22073, value = "JacOrbDefaultServant replay_completion for recoverId {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbDefaultServant_1(String arg0);

//	@Message(id = 22074, value = "JacOrbDefaultServant replay_completion for ObjectId {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbDefaultServant_2(String arg0);

	@Message(id = 22075, value = "JacOrbServant.replay_completion got exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCDefaultServant_3(@Cause() Throwable arg0);

//	@Message(id = 22076, value = "JacOrbRCManager: Created reference for tran {0} = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_1(String arg0, String arg1);

	@Message(id = 22077, value = "RCManager.makeRC did not make rcvco reference", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_2(@Cause() Throwable arg0);

	@Message(id = 22078, value = "RCManager could not find file in object store.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_3();

	@Message(id = 22079, value = "RCManager could not find file in object store during setup.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_4();

	@Message(id = 22080, value = "Unexpected exception during IOR setup", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_5(@Cause() Throwable arg0);

	@Message(id = 22081, value = "Failed to create poa for recoverycoordinators", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_1(@Cause() Throwable arg0);

//	@Message(id = 22082, value = "JacOrbRCServiceInit - set default servant and activated", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_2();

	@Message(id = 22083, value = "JacOrbRCServiceInit - Failed to start RC service", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_3(@Cause() Throwable arg0);

//	@Message(id = 22084, value = "Unable to create file ObjectId", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_4();

	@Message(id = 22085, value = "Unable to create file ObjectId - security problems", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_5();

	@Message(id = 22086, value = "Starting RecoveryServer ORB on port {0} and address {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6(String arg0, String arg1);

	@Message(id = 22087, value = "Sharing RecoveryServer ORB on port {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6a(String arg0);

	@Message(id = 22088, value = "Failed to create orb and poa for transactional objects", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_7(@Cause() Throwable arg0);

	@Message(id = 22089, value = "RootPOA is null. Initialization failed. Check no conflicting or duplicate service is running.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_8();

//	@Message(id = 22090, value = "JacOrb RecoveryCoordinator creator setup", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_JacOrbRecoveryInit_1();

	@Message(id = 22091, value = "Failed in ServerInitializer::post_init -", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_jacorb_recoverycoordinators_ServerInitializer_1(@Cause() Throwable arg0);

//	@Message(id = 22092, value = "Failed to obtain the service context -", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orbspecific_jacorb_recoverycoordinators_ServerRecoveryInterceptor_1();

	@Message(id = 22093, value = "Cannot create a codec of the required encoding.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_context_codeccreate();

	@Message(id = 22094, value = "{0} - a failure occured when getting {1} codec - unknown encoding.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_context_codecerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22095, value = "{0} - duplicate interceptor name for {1} when registering", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_context_duplicatename(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22096, value = "Context interceptor caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_context_error(@Cause() Throwable arg0);

	@Message(id = 22097, value = "Invalid portable interceptor transaction parameter!", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_context_invalidparam();

	@Message(id = 22098, value = "A server-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_context_sie();

	@Message(id = 22099, value = "{0} caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_context_srie(String arg0, @Cause() Throwable arg1);

	@Message(id = 22100, value = "Cannot create a codec of the required encoding.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_interposition_codeccreate();

	@Message(id = 22101, value = "{0} - a failure occured when getting {1} codec - unknown encoding.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_interposition_codecerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22102, value = "{0} - duplicate interceptor name for {1} when registering", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_interposition_duplicatename(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 22103, value = "Invalid portable interceptor transaction parameter!", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_interposition_invalidparam();

	@Message(id = 22104, value = "A server-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_interposition_sie();

	@Message(id = 22105, value = "{0} caught an unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_javaidl_interceptors_interposition_srie(String arg0, @Cause() Throwable arg1);

	@Message(id = 22106, value = "is not a valid unique identifier!", format = MESSAGE_FORMAT)
    public String get_orbspecific_otiderror();

	@Message(id = 22107, value = "{0} for {1} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_tficaught(String arg0, Uid arg1, @Cause() Throwable arg2);

	@Message(id = 22108, value = "{0} attempt to clean up failed with exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_orbspecific_tidyfail(String arg0, @Cause() Throwable arg1);

	@Message(id = 22109, value = "Resolution of OTS server failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_otsservererror(@Cause() Throwable arg0);

	@Message(id = 22110, value = "Resolution of OTS server failed - invalid name", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_otsserverfailed(@Cause() Throwable arg0);

//	@Message(id = 22111, value = "ExpiredContactScanner created, with expiry time of {0} seconds", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredContactScanner_1(String arg0);

//	@Message(id = 22112, value = "ExpiredContactScanner - scanning to remove items from before {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredContactScanner_2(String arg0);

	@Message(id = 22113, value = "Removing old contact item {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ExpiredContactScanner_3(Uid arg0);

//	@Message(id = 22114, value = "Expiry scan interval set to {0} seconds", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredContactScanner_4(String arg0);

//	@Message(id = 22115, value = "{0} has inappropriate value {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredContactScanner_5(String arg0, String arg1);

	@Message(id = 22116, value = "Could not locate supported ORB for RecoveryCoordinator initialisation.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoveryEnablement_1();

//	@Message(id = 22117, value = "Full crash recovery is not supported with this orb", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryEnablement_2();

//	@Message(id = 22118, value = "Set property {0}  =  {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryEnablement_3(String arg0, String arg1);

//	@Message(id = 22119, value = "RecoveryCoordinator service can only be provided in RecoveryManager", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryEnablement_4();

//	@Message(id = 22120, value = "ORB/OA initialisation failed: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryEnablement_5(String arg0);

	@Message(id = 22121, value = "The Recovery Service Initialisation failed}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoveryEnablement_6(@Cause() Throwable arg0);

//	@Message(id = 22122, value = "added ORBAttribute for recoveryCoordinatorInitialiser", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryInit_1();

//	@Message(id = 22123, value = "Full crash recovery is not supported with this orb", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryInit_2();

//	@Message(id = 22124, value = "added event handler  {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryInit_3(String arg0);

	@Message(id = 22125, value = "RecoveryCoordinator service can only be provided in RecoveryManager", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_recovery_RecoveryInit_4();

//	@Message(id = 22126, value = "ORB/OA initialisation failed: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryInit_5(String arg0);

	@Message(id = 22127, value = "Problem with storing process/factory link", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_1(@Cause() Throwable arg0);

	@Message(id = 22128, value = "Attempted to read FactoryContactItem of different version", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_2();

	@Message(id = 22129, value = "Stored IOR is not an ArjunaFactory", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_3();

	@Message(id = 22130, value = "Problem with restoring process/factory link", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_4(@Cause() Throwable arg0);

	@Message(id = 22131, value = "Problem with restoring process/factory link", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_5();

	@Message(id = 22132, value = "Problem with storing process/factory link", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_6();

	@Message(id = 22133, value = "Problem with removing contact item", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_FactoryContactItem_7(@Cause() Throwable arg0);

//	@Message(id = 22134, value = "RecoveryContactWriter() created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_RecoveryContactWriter_1();

//	@Message(id = 22135, value = "RecoveryContactWriter.connected( \" {0} \")", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_RecoveryContactWriter_2(String arg0);

//	@Message(id = 22136, value = "RecoveryContactWriter.connected - found ArjunaFactory", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_RecoveryContactWriter_3();

//	@Message(id = 22137, value = "StatusChecker.getStatus( {0} ) - current status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_1(String arg0, String arg1);

	@Message(id = 22138, value = "NoTransaction exception on trying to contact original process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_10();

	@Message(id = 22139, value = "CORBA exception on trying to contact original process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_11(@Cause() Throwable arg0);

	@Message(id = 22140, value = "Exception on trying to contact original process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void  warn_recovery_contact_StatusChecker_12(@Cause() Throwable arg0);

//	@Message(id = 22141, value = "StatusChecker.getStatus({0}) -  no factory, process previously dead", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_13(String arg0);

	@Message(id = 22142, value = "no known contactitem for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_14(Uid arg0);

	@Message(id = 22143, value = "surprise item in StatusChecker list for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_15(Uid arg0, @Cause() Throwable arg1);

//	@Message(id = 22144, value = "StatusChecker.getStatus( {0} ) - stored status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_2(String arg0, String arg1);

	@Message(id = 22145, value = "StatusChecked.getStatus - found intentions list for apparently unknown transaction: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_3(Uid arg0);

//	@Message(id = 22146, value = "StatusChecker.getStatus( {0} ) - Status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_4(String arg0, String arg1);

//	@Message(id = 22147, value = "StatusChecker.getStatus({0}) - NO_IMPLEMENT = dead", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_5(String arg0);

//	@Message(id = 22148, value = "StatusChecker.getStatus({0}) - TRANSIENT = dead", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_6(String arg0);

//	@Message(id = 22149, value = "StatusChecker.getStatus({0}) - COMM_FAILURE = live", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_7(String arg0);

//	@Message(id = 22150, value = "StatusChecker.getStatus({0}) - OBJECT_NOT_EXIST = dead", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_contact_StatusChecker_8(String arg0);

	@Message(id = 22151, value = "BAD_PARAM exception on trying to contact original process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_contact_StatusChecker_9();

	@Message(id = 22152, value = "{0} - being passed a null reference. Will ignore!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_rcnull(String arg0);

//	@Message(id = 22153, value = "GenericRecoveryCoordinator {0} constructed", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_recoverycoordinators_GenericRecoveryCoordinator_1(String arg0);

//	@Message(id = 22154, value = "GenericRecoveryCoordinator() constructing", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_recoverycoordinators_GenericRecoveryCoordinator_2();

//	@Message(id = 22155, value = "GenericRecoveryCoordinator - swapping Resource for RC {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_recoverycoordinators_GenericRecoveryCoordinator_4(String arg0);

	@Message(id = 22156, value = "GenericRecoveryCreator: Missing params to create", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoverycoordinators_GenericRecoveryCreator_1();

//	@Message(id = 22157, value = "RecoveryCoordinatorId: created RCkey {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_recoverycoordinators_RecoveryCoordinatorId_1(String arg0);

	@Message(id = 22158, value = "System exception when creating RecoveryCoordinator object key", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoverycoordinators_RecoveryCoordinatorId_2(@Cause() Throwable arg0);

	@Message(id = 22159, value = "RecoveryCoordinatorId could not decode data {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoverycoordinators_RecoveryCoordinatorId_3(String arg0);

//	@Message(id = 22160, value = "ResourceCompletor.rollback() - rollback failed: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_recoverycoordinators_ResourceCompletor_1(String arg0);

	@Message(id = 22161, value = "Failure recovery not supported for this ORB.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoveryinit_1();

//	@Message(id = 22162, value = "AssumedCompleteServerTransaction {0} created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_AssumedCompleteServerTransaction_1(String arg0);

//	@Message(id = 22163, value = "AssumedCompleteTransaction {0} created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_AssumedCompleteTransaction_1(String arg0);

//	@Message(id = 22164, value = "", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_CachedRecoveredTransaction_1();

//	@Message(id = 22165, value = "CachedRecoveredTransaction.originalBusy - told status is {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_CachedRecoveredTransaction_2(String arg0);

//	@Message(id = 22166, value = "RecoveredServerTransaction {0} created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_1(String arg0);

	@Message(id = 22167, value = "Got TRANSIENT from ORB for tx {0} and assuming OBJECT_NOT_EXIST", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_10(Uid arg0);

//	@Message(id = 22168, value = "RecoveredServerTransaction.getStatusFromParent - replay_completion got object_not_exist = {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_11(String arg0);

	@Message(id = 22169, value = "RecoveredServerTransaction: caught NotPrepared", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_12();

	@Message(id = 22170, value = "RecoveredServerTransaction: caught unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_13(@Cause() Throwable arg0);

	@Message(id = 22171, value = "RecoveredServerTransaction: {0} is invalid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_14(Uid arg0);

//	@Message(id = 22172, value = "RecoveredServerTransaction:getStatusFromParent - no recovcoord or status not prepared", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_15();

//	@Message(id = 22173, value = "\"RecoveredServerTransaction.unpackHeader - txid = {0} and processUid = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_16(String arg0, String arg1);

//	@Message(id = 22174, value = "RecoveredServerTransaction - activate of {0} failed with {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_17(String arg0, String arg1);

	@Message(id = 22175, value = "RecoveredServerTransaction - activate of {0} failed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_2(Uid arg0);

//	@Message(id = 22176, value = "RecoveredServerTransaction.replayPhase2({0}) - status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_4(String arg0, String arg1);

//	@Message(id = 22177, value = "RecoveredServerTransaction.replayPhase2({0}) - status after contacting parent = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_5(String arg0, String arg1);

	@Message(id = 22178, value = "ServerTransaction {0} unable determine status - retry later", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_RecoveredServerTransaction_6(Uid arg0);

	@Message(id = 22179, value = "RecoveredServerTransaction.replayPhase2: unexpected Status: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredServerTransaction_7(String arg0);

//	@Message(id = 22180, value = "RecoveredServerTransaction.replayPhase2: ({0}) finished", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_8(String arg0);

//	@Message(id = 22181, value = "RecoveredServerTransaction.getStatusFromParent - replay_completion status = {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredServerTransaction_9(String arg0);

//	@Message(id = 22182, value = "RecoveredTransaction {0} created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredTransaction_1(String arg0);

	@Message(id = 22183, value = "RecoveredTransaction activate of {0} failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredTransaction_2(Uid arg0);

	@Message(id = 22184, value = "RecoveredTransaction activate of {0} failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredTransaction_3(Uid arg0, @Cause() Throwable arg1);

//	@Message(id = 22185, value = "RecoveredTransaction.replayPhase2 ({0}) - status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredTransaction_5(String arg0, String arg1);

	@Message(id = 22186, value = "RecoveredTransaction.replayPhase2 for {0} failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredTransaction_6(String arg0);

//	@Message(id = 22187, value = "RecoveredTransaction.replayPhase2 ({0}) finished", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_RecoveredTransaction_7(String arg0);

	@Message(id = 22188, value = "RecoveredTransaction.removeOldStoreEntry - problem", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_RecoveredTransaction_8(@Cause() Throwable arg0);

//	@Message(id = 22189, value = "ServerTransactionRecoveryModule created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_ServerTransactionRecoveryModule_1();

	@Message(id = 22190, value = "ServerTransactionRecoveryModule - First Pass", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_ServerTransactionRecoveryModule_3();

	@Message(id = 22191, value = "ServerTransactionRecoveryModule - Second Pass", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_ServerTransactionRecoveryModule_4();

	@Message(id = 22192, value = "ServerTransactionRecoveryModule - Transaction {0} still in ActionStore", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_ServerTransactionRecoveryModule_5(Uid arg0);

//	@Message(id = 22193, value = "ServerTransactionRecoveryModule - Transaction {0} still in state unknown (?).", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_ServerTransactionRecoveryModule_6(String arg0);

//	@Message(id = 22194, value = "ServerTransactionRecoveryModule - Transaction {0} is not in object store - assumed completed", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_ServerTransactionRecoveryModule_7(String arg0);

//	@Message(id = 22195, value = "Activated transaction {0} status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_ServerTransactionRecoveryModule_8(String arg0, String arg1);

//	@Message(id = 22196, value = "Transaction {0} still busy", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_ServerTransactionRecoveryModule_9(String arg0);

//	@Message(id = 22197, value = "TopLevelTransactionRecoveryModule created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TopLevelTransactionRecoveryModule_1();

//	@Message(id = 22198, value = "TopLevelTransactionRecoveryModule destoryed", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TopLevelTransactionRecoveryModule_2();

	@Message(id = 22199, value = "TopLevelTransactionRecoveryModule First Pass", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TopLevelTransactionRecoveryModule_3();

	@Message(id = 22200, value = "TopLevelTransactionRecoveryModule Second Pass", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TopLevelTransactionRecoveryModule_4();

//	@Message(id = 22201, value = "Transaction {0} previously assumed complete", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCacheItem_1(String arg0);

	@Message(id = 22202, value = "TransactionCacheItem.loadTransaction - unknown type: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_TransactionCacheItem_2(String arg0);

//	@Message(id = 22203, value = "asking the tran for original status", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_1();

//	@Message(id = 22204, value = "no transaction in cache so not asking for original status", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_2();

//	@Message(id = 22205, value = "Transaction {0} assumed complete - changing type.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_3(String arg0);

	@Message(id = 22206, value = "Transaction {0} assumed complete - will not poll any more", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TransactionCache_4(Uid arg0);

	@Message(id = 22207, value = "Transaction {0} recovery completed", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TransactionCache_5(Uid arg0);

//	@Message(id = 22208, value = "TransactionCache.remove {0}: transaction not in cache", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_6(String arg0);

//	@Message(id = 22209, value = "TransactionCache.remove {0}: removed transaction from cache", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_7(String arg0);

//	@Message(id = 22210, value = "Non-integer value for property {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionCache_8(String arg0);

//	@Message(id = 22211, value = "TransactionRecoveryModule created", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_1();

//	@Message(id = 22212, value = "Transaction {0} still busy", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_10(String arg0);

	@Message(id = 22213, value = "TransactionRecoveryModule.periodicWorkFirstPass()", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TransactionRecoveryModule_11();

	@Message(id = 22214, value = "TransactionRecoveryModule.periodicWorkSecondPass()", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TransactionRecoveryModule_12();

	@Message(id = 22215, value = "TransactionRecoveryModule: transaction type not set", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_TransactionRecoveryModule_2();

//	@Message(id = 22216, value = "TransactionRecoveryModule: scanning for {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_3(String arg0);

	@Message(id = 22217, value = "TransactionRecoveryModule: Object store exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_transactions_TransactionRecoveryModule_4(@Cause() Throwable arg0);

//	@Message(id = 22218, value = "found transaction  {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_5(String arg0);

	@Message(id = 22219, value = "Transaction {0} still in ActionStore", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_transactions_TransactionRecoveryModule_6(Uid arg0);

//	@Message(id = 22220, value = "Transaction {0} in state unknown (?).", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_7(String arg0);

//	@Message(id = 22221, value = "Transaction {0} is not in object store - assumed completed", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_8(String arg0);

//	@Message(id = 22222, value = "Activated transaction {0} status = {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_transactions_TransactionRecoveryModule_9(String arg0, String arg1);

	@Message(id = 22223, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errgenerr(String arg0, @Cause() Throwable arg1);

	@Message(id = 22224, value = "{0} - no parent!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errnoparent(String arg0);

	@Message(id = 22225, value = "{0} called without a resource reference!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errnores(String arg0);

	@Message(id = 22226, value = "{0} failed. Returning default value: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errsavefail(String arg0, String arg1);

	@Message(id = 22227, value = "{0} called illegally!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errsetvalue(String arg0);

	@Message(id = 22228, value = "{0} failed. Returning default value: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_errtypefail(String arg0, String arg1);

	@Message(id = 22229, value = "{0} has no parent transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_noparent(Uid arg0);

	@Message(id = 22230, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_rrcaught(String arg0, @Cause() Throwable arg1);

	@Message(id = 22231, value = "{0} called illegally.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_rrillegalvalue(String arg0);

	@Message(id = 22232, value = "{0} called without a resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_rrinvalid(String arg0);

	@Message(id = 22233, value = "{0} caught unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_eicaughtexception(String arg0, @Cause() Throwable arg1);

	@Message(id = 22234, value = "{0} called multiple times.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_excalledagain(String arg0);

	@Message(id = 22235, value = "Could not rollback transaction {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_abortfail(String arg0);

	@Message(id = 22236, value = "Could not rollback transaction {0} as it does not exist!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_abortfailnoexist(String arg0);

	@Message(id = 22237, value = "{0} - cannot rollback {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atcannotabort(String arg0, String arg1);

//	@Message(id = 22238, value = "{0} - current transaction is null!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_extensions_atcurrenttxnull(String arg0);

	@Message(id = 22239, value = "{0} caught unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atgenerror(String arg0, @Cause() Throwable arg1);

	@Message(id = 22240, value = "{0} - no transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atnovalidtx(String arg0);

	@Message(id = 22241, value = "{0} - terminated out of sequence {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atoutofseq(String arg0, String arg1);

	@Message(id = 22242, value = "{0} - running atomic transaction going out of scope. Will roll back. {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atscope(String arg0, Uid arg1);

	@Message(id = 22243, value = "{0} - transaction unavailable.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atunavailable(String arg0);

	@Message(id = 22244, value = "Will roll back. Current transaction is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_atwillabort(String arg0);

	@Message(id = 22245, value = "Cannot determine transaction name!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_namefail(@Cause() Throwable arg0);

	@Message(id = 22246, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_threadasserror(String arg0, @Cause() Throwable arg1);

	@Message(id = 22247, value = "Top-level transaction going out of scope with nested transaction {0} still set.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_extensions_tltnestedscope(String arg0);

	@Message(id = 22248, value = "{0} - could not unregister from transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_exunregfail(String arg0);

	@Message(id = 22249, value = "{0} - could not resume transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_thread_resumefailed(String arg0, @Cause() Throwable arg1);

	@Message(id = 22250, value = "could not resume transaction:", format = MESSAGE_FORMAT)
	public String get_thread_resumefailederror();

	@Message(id = 22251, value = "The ORBManager is already associated with an ORB/OA.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_utils_ORBSetup_orbalreadyset();

    @Message(id = 22252, value = "Failed to remove old ObjectStore entry", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_recoveredServerTransaction_removeOldStoreEntry(@Cause() Throwable arg0);
    
	@Message(id = 22253, value = "A client-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_jacorb_interceptors_context_cie();

    @Message(id = 22254, value = "A client-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
    public String get_orbspecific_jacorb_interceptors_interposition_cie();

	@Message(id = 22255, value = "A client-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_context_cie();

    @Message(id = 22256, value = "A client-side request interceptor already exists with that name.", format = MESSAGE_FORMAT)
	public String get_orbspecific_javaidl_interceptors_interposition_cie();

    @Message(id = 22257, value = "{0} - unknown interposition type: {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_orbspecific_coordinator_ipunknown(String arg0, String arg1);

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
