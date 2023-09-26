/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.webservices.logging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import org.jboss.logging.Logger;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import jakarta.xml.bind.JAXBElement;

public class WSTLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.wst");
    public static final wstI18NLogger i18NLogger = Logger.getMessageLogger(wstI18NLogger.class, "com.arjuna.wst");

    /**
     * Printing trace of operation. The operation will be added to {@link com.arjuna.services.framework.task.TaskManager} queue.
     */
    public static void traceOperation(String message, Notification notification, MAP inboundMap, ArjunaContext arjunaContext) {
        String notificationPart = null, inboundMapPart = null;
        if (notification != null) {
            notificationPart = String.format("[properties: %s, other attributes: %s]",
                    notification.getAny(), notification.getOtherAttributes());
        }
        WSTLogger.logger.tracef("%s : arjuna id: %s, notification: %s, inboundMap: %s", message,
                (arjunaContext != null ? arjunaContext.getInstanceIdentifier() : arjunaContext), notificationPart, formatMAP(inboundMap));

    }

    /**
     * Printing trace of fault port type processing.
     */
    public static void traceFault(String message, SoapFault soapFault, MAP inboundMap, ArjunaContext arjunaContext) {

        WSTLogger.logger.tracef("%s : arjuna id: %s, fault: %s, inboundMap: %s", message,
                (arjunaContext != null ? arjunaContext.getInstanceIdentifier() : arjunaContext), soapFault, formatMAP(inboundMap));

    }

    private static String formatMAP(MAP inboundMap) {
        if (inboundMap != null) {
            StringBuilder referenceParameters = new StringBuilder();
            if (inboundMap.getReferenceParameters() != null) {
                inboundMap.getReferenceParameters().stream().filter(t -> t instanceof jakarta.xml.bind.JAXBElement)
                        .forEach(t -> {
                            JAXBElement jaxb = (JAXBElement) t;
                            referenceParameters.append("(name:").append(jaxb.getName()).append(",value:").append(jaxb.getValue()).append(");");
                        });
            }
            return String.format("[message: %s, action: %s, from: %s, to: %s, reply to: %s, fault to: %s, relates: %s, ref params: %s]",
                    inboundMap.getMessageID(), inboundMap.getAction(), (inboundMap.getFrom() != null ? inboundMap.getFrom().getAddress() : inboundMap.getFrom()),
                    inboundMap.getTo(), (inboundMap.getReplyTo() != null ? inboundMap.getReplyTo().getAddress() : inboundMap.getReplyTo()),
                    (inboundMap.getFaultTo() != null ? inboundMap.getFaultTo().getAddress() : inboundMap.getFaultTo()),
                    (inboundMap.getRelatesTo() != null ? inboundMap.getRelatesTo().getRelatesTo() : inboundMap.getRelatesTo()),
                    referenceParameters.toString());
        }
        return "";
    }
}