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
package com.arjuna.webservices.logging;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the wsc module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface wscI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

	@Message(id = 42001, value = "Unhandled error executing task", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_services_framework_task_TaskWorker_run_1();

	@Message(id = 42002, value = "Invalid fault type enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_SoapFaultType_1(String arg0);

	@Message(id = 42003, value = "setNamespaceContext unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_1();

	@Message(id = 42004, value = "writeEndDocument unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_10();

	@Message(id = 42005, value = "close unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_11();

	@Message(id = 42006, value = "writeComment unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_2();

	@Message(id = 42007, value = "writeProcessingInstruction unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_3();

	@Message(id = 42008, value = "writeProcessingInstruction unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_4();

	@Message(id = 42009, value = "writeEntityRef unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_5();

	@Message(id = 42010, value = "writeDTD unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_6();

	@Message(id = 42011, value = "writeStartDocument unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_7();

	@Message(id = 42012, value = "writeStartDocument unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_8();

	@Message(id = 42013, value = "writeStartDocument unsupported", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_BaseXMLStreamWriter_9();

	@Message(id = 42014, value = "End of stream", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_1();

	@Message(id = 42015, value = "Unexpected child node: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_2(String arg0);

	@Message(id = 42016, value = "Unexpected type: [0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_3();

	@Message(id = 42017, value = "Unexpected type: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_4(String arg0);

	@Message(id = 42018, value = "Unexpected event type", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_5();

	@Message(id = 42019, value = "Unsupported operation", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_6();

	@Message(id = 42020, value = "Unsupported operation", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_DOMXMLStreamReader_7();

	@Message(id = 42021, value = "CData sections not currently supported.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_adapters_SAAJXMLStreamWriter_1();

	@Message(id = 42022, value = "Service {0} received unexpected fault: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_base_handlers_LoggingFaultHandler_1(String arg0, String arg1);

	@Message(id = 42023, value = "Unexpected start element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_Soap11Details_1(String arg0);

	@Message(id = 42024, value = "Unexpected start element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_Soap12Details_1(String arg0);

	@Message(id = 42025, value = "NotUnderstood elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_SoapNotUnderstoodType_1();

	@Message(id = 42026, value = "Unexpected element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_SoapParser_1(String arg0);

	@Message(id = 42027, value = "Unexpected body element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_SoapParser_2(String arg0);

	@Message(id = 42028, value = "Did not understand header: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_soap_SoapParser_3(String arg0);

	@Message(id = 42029, value = "Encountered unexpected event type: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_stax_ParsingSupport_1(String arg0);

	@Message(id = 42030, value = "Text elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_stax_TextElement_1();

	@Message(id = 42031, value = "No response from RPC request", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_transport_http_HttpClient_1();

	@Message(id = 42032, value = "Invalid destination URL", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_transport_http_HttpClient_2();

	@Message(id = 42033, value = "Unsupported URL type, not HTTP or HTTPS", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_transport_http_HttpClient_3();

	@Message(id = 42034, value = "Invalid response code returned: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_transport_http_HttpClient_4(String arg0);

	@Message(id = 42035, value = "Unexpected end element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_util_StreamHelper_1(String arg0);

	@Message(id = 42036, value = "Unexpected end of document reached", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_util_StreamHelper_2();

	@Message(id = 42037, value = "Unexpected start element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_util_StreamHelper_3(String arg0);

	@Message(id = 42038, value = "Addressing context is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr_AddressingContext_1();

	@Message(id = 42039, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr_AddressingContext_2(String arg0);

	@Message(id = 42040, value = "Invalid QName value for attributed QName", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr_AttributedQNameType_1();

	@Message(id = 42041, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr_EndpointReferenceType_1(String arg0);

	@Message(id = 42042, value = "Addressing context is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_AddressingContext_1();

	@Message(id = 42043, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_AddressingContext_2(String arg0);

	@Message(id = 42044, value = "Unexpected second element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_AttributedAnyType_1(String arg0);

	@Message(id = 42045, value = "Invalid QName value for attributed QName", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_AttributedQNameType_1();

	@Message(id = 42046, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_EndpointReferenceType_1(String arg0);

	@Message(id = 42047, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_ProblemActionType_1(String arg0);

	@Message(id = 42048, value = "Addressing context does not specify destination.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_client_WSAddr2005Client_1();

	@Message(id = 42049, value = "Invalid destination specified in addressing context.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_client_WSAddr2005Client_2();

	@Message(id = 42050, value = "No SOAP client registered for scheme: {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_client_WSAddr2005Client_3(String arg0);

	@Message(id = 42051, value = "Invalid replyTo specified in addressing context.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_client_WSAddr2005Client_4();

	@Message(id = 42052, value = "Unexpected SOAP message type returned.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_client_WSAddr2005Client_5();

	@Message(id = 42053, value = "Unhandled SOAP fault during asynchronous execution of service.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsaddr2005_handlers_AddressingInterceptorHandler_3();

	@Message(id = 42054, value = "Arjuna context is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsarj_ArjunaContext_1();

	@Message(id = 42055, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsarj_ArjunaContext_2(String arg0);

	@Message(id = 42056, value = "InstanceIdentifier elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsarj_InstanceIdentifier_1();

	@Message(id = 42057, value = "non numerical value: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_AttributedUnsignedIntType_1(String arg0);

	@Message(id = 42058, value = "Coordination Context is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CoordinationContextType_1();

	@Message(id = 42059, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CoordinationContextType_2(String arg0);

	@Message(id = 42060, value = "Create Coordination Context Response is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CreateCoordinationContextResponseType_1();

	@Message(id = 42061, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CreateCoordinationContextResponseType_2(String arg0);

	@Message(id = 42062, value = "Create Coordination Context is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CreateCoordinationContextType_1();

	@Message(id = 42063, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_CreateCoordinationContextType_2(String arg0);

	@Message(id = 42064, value = "Register is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_RegisterResponseType_1();

	@Message(id = 42065, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_RegisterResponseType_2(String arg0);

	@Message(id = 42066, value = "Register is not valid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_RegisterType_1();

	@Message(id = 42067, value = "Unexpected element name: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wscoor_RegisterType_2(String arg0);

	@Message(id = 42068, value = "Unexpected end element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_util_StreamHelper_1(String arg0);

	@Message(id = 42069, value = "Unexpected end of document reached", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_util_StreamHelper_2();

	@Message(id = 42070, value = "Unexpected start element: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_util_StreamHelper_3(String arg0);

	@Message(id = 42071, value = "Callback execution failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_ActivationCoordinator_1();

	@Message(id = 42072, value = "Callback wasn't triggered", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_ActivationCoordinator_2();

	@Message(id = 42073, value = "Callback execution failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_RegistrationCoordinator_1();

	@Message(id = 42074, value = "Callback wasn't triggered", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_RegistrationCoordinator_2();

	@Message(id = 42075, value = "Invalid create coordination context parameters", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ActivationCoordinatorProcessorImpl_1();

	@Message(id = 42076, value = "Participant already registered", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_RegistrationCoordinatorProcessorImpl_1();

	@Message(id = 42077, value = "Invalid protocol identifier", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_RegistrationCoordinatorProcessorImpl_2();

	@Message(id = 42078, value = "Invalid coordination context state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_RegistrationCoordinatorProcessorImpl_3();

	@Message(id = 42079, value = "Unknown activity identifier", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_RegistrationCoordinatorProcessorImpl_4();

	@Message(id = 42080, value = "Invalid create coordination context parameters", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wsc11_messaging_ActivationCoordinatorProcessorImpl_1();

	@Message(id = 42081, value = "Participant already registered", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wsc11_messaging_RegistrationCoordinatorProcessorImpl_1();

	@Message(id = 42082, value = "Invalid protocol identifier", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wsc11_messaging_RegistrationCoordinatorProcessorImpl_2();

	@Message(id = 42083, value = "Invalid coordination context state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wsc11_messaging_RegistrationCoordinatorProcessorImpl_3();

	@Message(id = 42084, value = "Unknown activity identifier", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wsc11_messaging_RegistrationCoordinatorProcessorImpl_4();

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in numeric sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
     */
}
