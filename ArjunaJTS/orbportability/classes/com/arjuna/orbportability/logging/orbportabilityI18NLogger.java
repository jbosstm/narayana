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
package com.arjuna.orbportability.logging;

import org.jboss.logging.*;

import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the orbportability module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface orbportabilityI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 21001, value = "{0} caught exception whilst initialising Object Adapter.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_OA_caughtexception(String arg0, Throwable arg1);

	@Message(id = 21002, value = "{0}: exception caught for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_OA_exceptioncaughtforobj(String arg0, String arg1, Throwable arg2);

	@Message(id = 21003, value = "caught org.omg.CORBA.INITIALIZE whilst initialising Object Adapter. Check another ORB/service is not active on same port.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_OA_initialize();

	@Message(id = 21004, value = "{0} - invalid POA: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_OA_invalidpoa(String arg0, String arg1);

	@Message(id = 21005, value = "OA.createPOA - createPOA called without OA being initialised", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_OA_oanotinitialised();

	@Message(id = 21006, value = "OA.initPOA called without initialised ORB.", format = MESSAGE_FORMAT)
	public String get_OA_uninitialsedorb();

//	@Message(id = 21007, value = "Cannot find default ORB configuration file {0} in the classpath", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_ORB_cannotfinddefaultorbconfig(String arg0);

//	@Message(id = 21008, value = "{0} caught exception: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_ORB_caughtexception(String arg0, String arg1);

//	@Message(id = 21009, value = "The ORB configuration specified in {0} is invalid: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_ORB_invalidorbconfig(String arg0, String arg1);

	@Message(id = 21010, value = "ORBInfo ORB specific class creation failed with exception", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_ORBInfo_creationfailed(Throwable arg0);

	@Message(id = 21011, value = "ORBInfo ORB specific class creation failed - unable to find supported ORB", format = MESSAGE_FORMAT)
    @LogMessage(level = FATAL)
	public void fatal_ORBInfo_unsupportedorb(Throwable arg0);

//	@Message(id = 21012, value = "{0} - could not open config file: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_Services_openfailure(String arg0, String arg1);

	@Message(id = 21013, value = "{0} - {1} option not supported by ORB.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_Services_optionnotsupported(String arg0, String arg1);

//	@Message(id = 21014, value = "Services.getService - resolve_initial_references on {0} failed: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_Services_resolvereffailed(String arg0, String arg1);

	@Message(id = 21015, value = "Services.getService - could not find service: {0} in configuration file: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_Services_servicenotfound(String arg0, String arg1);

	@Message(id = 21016, value = "{0} Suspect entry in configuration file: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_Services_suspectentry(String arg0, String arg1);

	@Message(id = 21017, value = "{0} - caught unexpected exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_Services_unexpectedexception(String arg0, Throwable arg1);

	@Message(id = 21018, value = "Services.getService - {0} option not supported by ORB.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_Services_unsupportedoption(String arg0);

	@Message(id = 21019, value = "{0} - invalid bind mechanism in properties file", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Configuration_bindDefault_invalidbind(String arg0);

	@Message(id = 21020, value = "{0} - caught exception for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_event_EventManager_caughtexceptionfor(String arg0, String arg1, Throwable arg2);

	@Message(id = 21021, value = "{0} - for: {1} threw exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_event_EventManager_forhandlethrewexception(String arg0, String arg1, Throwable arg2);

//	@Message(id = 21022, value = "{0} - no value for: {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_event_EventManager_novalue(String arg0, String arg1);

	@Message(id = 21023, value = "{0} called without root POA.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_internal_orbspecific_oa_implementations(String arg0);

	@Message(id = 21024, value = "{0} - could not find class {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_internal_utils_InitLoader_couldnotfindclass(String arg0, String arg1);

	@Message(id = 21025, value = "Exception whilst loading {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_internal_utils_InitLoader_exception(String arg0, Throwable arg1);

	@Message(id = 21026, value = "{0} - attempt to initialise {1} with null class name!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_internal_utils_InitLoader_initfailed(String arg0, String arg1);

	@Message(id = 21027, value = "Loading {0} class - {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_internal_utils_InitLoader_loading(String arg0, String arg1);

//	@Message(id = 21028, value = "OA ORB specific class creation failed with: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_oa_core_OA_caughtexception(String arg0);

//	@Message(id = 21029, value = "OA ORB specific class creation failed - unable to find supported ORB", format = MESSAGE_FORMAT)
//	public String get_oa_core_OA_nosupportedorb();

//	@Message(id = 21030, value = "ORB specific class creation failed with: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orb_core_ORB_caughtexception(String arg0);

//	@Message(id = 21031, value = "ORB specific class creation failed - unable to find supported ORB", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_orb_core_ORB_unsupportedorb();

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
