/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.jatmibroker;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyNoTpreturnService;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyTpcallTPEOTYPEService;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyTpcallTPESVCFAILService;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyTpcallTPETIMEService;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyTprecvTPEVDISCONIMMService;
import org.jboss.narayana.blacktie.jatmibroker.tx.services.RollbackOnlyTprecvTPEVSVCFAILService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.server.BlackTieServer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.SpecQuickstartOneService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.SpecQuickstartTwoService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPACallService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPCallXCTypeService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPCallXCommonService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPCallXOctetService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPCancelService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPConnectService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPConversationService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPConversationShortService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPDisconService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPGetRplyOneService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPGetRplyService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPGetRplyTPNOBLOCKService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPGetRplyTwoService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPRecvService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPReturnOpenSession1Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPReturnOpenSession2Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPReturnService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPReturnTpurcodeService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPSendNonTPCONVService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPSendService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPSendTPSendOnlyService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPServiceService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TTLService;

public class RunServer {

    private BlackTieServer server;

    public void serverinit() throws ConfigurationException, ConnectionException {
        this.server = new BlackTieServer("myserv");
    }

    public void serverdone() throws ConnectionException {
        server.shutdown();
    }

    public void tpadvertiseBAR() {
    }

    public void tpadvertiseLOOPY() {
    }

    public void tpadvertiseDEBIT() throws ConnectionException {
        this.server.tpadvertise("TestOne", SpecQuickstartOneService.class.getName());
    }

    public void tpadvertiseCREDIT() throws ConnectionException {
        this.server.tpadvertise("TestTwo", SpecQuickstartOneService.class.getName());
    }

    public void tpadvertiseINQUIRY() throws ConnectionException {
        this.server.tpadvertise(getServiceNameINQUIRY(), SpecQuickstartTwoService.class.getName());
    }

    public void tpadvertiseTestTPACall() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPACallService.class.getName());
    }

    public void tpadvertisetpcallXOctet() throws ConnectionException {
        this.server.tpadvertise(getServiceNametpcallXOctet(), TPCallXOctetService.class.getName());
    }

    public void tpadvertisetpcallXOctetZero() throws ConnectionException {
    }

    public void tpadvertisetpcallXCommon() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPCallXCommonService.class.getName());
    }

    public void tpadvertisetpcallXCType() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPCallXCTypeService.class.getName());
    }

    public void tpadvertiseTestTPCancel() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPCancelService.class.getName());
    }

    public void tpadvertiseTestTPConnect() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPConnect(), TPConnectService.class.getName());
    }

    public void tpadvertiseTestTPConversation() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPConversation(), TPConversationService.class.getName());
    }

    public void tpadvertiseTestTPConversa2() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPConversa2(), TPConversationShortService.class.getName());
    }

    public void tpadvertiseTestTPDiscon() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPDiscon(), TPDisconService.class.getName());
    }

    public void tpadvertiseTestTPFree() throws ConnectionException {
    }

    public void tpadvertiseTestTPGetrply() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPGetRplyService.class.getName());
    }

    public void tpadvertiseTestTPRecv() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPRecv(), TPRecvService.class.getName());
    }

    public void tpadvertiseTestTPReturn() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPReturn(), TPReturnService.class.getName());
    }

    public void tpadvertiseTestTPReturn2() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPReturn2(), TPReturnTpurcodeService.class.getName());
    }

    public void tpadvertiseTestTPReturn3() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPReturn(), TPReturnOpenSession1Service.class.getName());

    }

    public void tpadvertiseTestTPReturn4() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPReturn2(), TPReturnOpenSession2Service.class.getName());

    }

    public void tpadvertiseTestTPSend() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPSend(), TPSendService.class.getName());
    }

    public void tpadvertiseTestTPSendTPSendOnly() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPSendTPSendOnly(), TPSendTPSendOnlyService.class.getName());
    }

    public void tpadvertiseTestTPService() throws ConnectionException {
        this.server.tpadvertise("TestOne", TPServiceService.class.getName());
    }

    public void tpadvertiseTestTPUnadvertise() throws ConnectionException {
    }

    public void tpadvertiseTX1() throws ConnectionException {
    }

    public void tpadvertiseTX2() throws ConnectionException {
    }

    public void tpadvertiseTTL() throws ConnectionException {
        this.server.tpadvertise("TestOne", TTLService.class.getName());
    }

    // SERVICE NAMES
    public static String getServiceNameBAR() {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public static String getServiceNameLOOPY() {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public static String getServiceNameDEBIT() {
        return "TestOne";
    }

    public static String getServiceNameCREDIT() {
        return "TestTwo";
    }

    public static String getServiceNameINQUIRY() {
        return "ConvService";
    }

    public static String getServiceNameTestTPACall() {
        return "TestOne";
    }

    public static String getServiceNametpcallXOctet() {
        return "TestOne";
    }

    public static String getServiceNametpcallXOctetZero() {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public static String getServiceNametpcallXCommon() {
        return "TestOne";
    }

    public static String getServiceNametpcallXCType() {
        return "TestOne";
    }

    public static String getServiceNameTestTPCancel() {
        return "TestOne";
    }

    public static String getServiceNameTestTPConnect() {
        return "ConvService";
    }

    public static String getServiceNameTestTPConversation() {
        return "ConvService";
    }

    public static String getServiceNameTestTPDiscon() {
        return "ConvService";
    }

    public static String getServiceNameTestTPConversa2() {
        return "ConvService";
    }

    public static String getServiceNameTestTPGetrply() {
        return "TestOne";
    }

    public static String getServiceNameTestTPRecv() {
        return "ConvService";
    }

    public static String getServiceNameTestTPReturn() {
        return "TestOne";
    }

    public static String getServiceNameTestTPReturn2() {
        return "TestTwo";
    }

    public static String getServiceNameTestTPSend() {
        return "ConvService";
    }

    public static String getServiceNameTestTPSendTPSendOnly() {
        return "ConvService";
    }

    public static String getServiceNameTestTPService() {
        return "TestOne";
    }

    public static String getServiceNameTestTPUnadvertise() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public static String getServiceNameTX1() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public static String getServiceNameTX2() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public static String getServiceNameTTL() {
        return "TestOne";
    }

    public static String getServiceNameTestRollbackOnly() {
        return "TestOne";
        // return "TestRbkOnly";
    }

    public static String getServiceNameTestTPGetrplyOne() {
        return "TestOne";
    }

    public static String getServiceNameTestTPGetrplyTwo() {
        return "TestTwo";
    }

    public void tpadvertiseTestRollbackOnlyTpcallTPETIMEService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly(), RollbackOnlyTpcallTPETIMEService.class.getName());
    }

    public void tpadvertiseTestTpcallTPEOTYPEService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly(), RollbackOnlyTpcallTPEOTYPEService.class.getName());

    }

    public void tpadvertiseTestRollbackOnlyTpcallTPESVCFAILService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly(), RollbackOnlyTpcallTPESVCFAILService.class.getName());
    }

    public void tpadvertiseTestRollbackOnlyTprecvTPEVDISCONIMMService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly2(), RollbackOnlyTprecvTPEVDISCONIMMService.class.getName());
    }

    public void tpadvertiseTestRollbackOnlyTprecvTPEVSVCFAILService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly2(), RollbackOnlyTprecvTPEVSVCFAILService.class.getName());
    }

    public void tpadvertiseTestRollbackOnlyNoTpreturnService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestRollbackOnly(), RollbackOnlyNoTpreturnService.class.getName());
    }

    public void tpadvertiseTestTPGetrplyOne() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPGetrplyOne(), TPGetRplyOneService.class.getName());

    }

    public void tpadvertiseTestTPGetrplyTwo() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTPGetrplyTwo(), TPGetRplyTwoService.class.getName());

    }

    public void tpadvertiseTestTPSendNonTPCONVService() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTPSendNonTPCONVService(), TPSendNonTPCONVService.class.getName());
    }

    public static String getServiceNameTPSendNonTPCONVService() {
        return "TestOne";
    }

    public static String getServiceNameTestRollbackOnly2() {
        return "ConvService";
    }

    public void tpadvertiseTestTPGetRplyTPNOBLOCK() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTPGetRplyTPNOBLOCK(), TPGetRplyTPNOBLOCKService.class.getName());
    }

    public String getServiceNameTPGetRplyTPNOBLOCK() {
        return "TestOne";
    }

    public void tpadvertiseTestTopic() throws ConnectionException {
        this.server.tpadvertise(getServiceNameTestTopic(), TPServiceService.class.getName());
    }

    public static String getServiceNameTestTopic() {
        return "JAVA_Topic";
    }
}
