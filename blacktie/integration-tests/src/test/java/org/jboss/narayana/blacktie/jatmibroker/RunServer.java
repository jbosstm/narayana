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

public class RunServer {

    static {
        System.loadLibrary("testsuite");
    }

    public native void serverinit();

    public native void serverdone();

    public native void tpadvertiseBAR();

    public native void tpadvertiseLOOPY();

    public native void tpadvertiseDEBIT();

    public native void tpadvertiseCREDIT();

    public native void tpadvertiseINQUIRY();

    public native void tpadvertiseTestTPACall();

    public native void tpadvertisetpcallXOctet();

    public native void tpadvertisetpcallXOctetZero();

    public native void tpadvertisetpcallXCommon();

    public native void tpadvertisetpcallXCType();

    public native void tpadvertiseTestTPCancel();

    public native void tpadvertiseTestTPConnect();

    public native void tpadvertiseTestTPConversation();

    public native void tpadvertiseTestTPConversa2();

    public native void tpadvertiseTestTPDiscon();

    public native void tpadvertiseTestTPFree();

    public native void tpadvertiseTestTPGetrply();

    public native void tpadvertiseTestTPRecv();

    public native void tpadvertiseTestTPReturn();

    public native void tpadvertiseTestTPReturn2();

    public native void tpadvertiseTestTPSend();

    public native void tpadvertiseTestTPService();

    public native void tpadvertiseTestTPUnadvertise();

    public native void tpadvertiseTX1();

    public native void tpadvertiseTX2();

    // SERVICE NAMES
    public static String getServiceNameBAR() {
        return "BAR";
    }

    public static String getServiceNameLOOPY() {
        return "LOOPY";
    }

    public static String getServiceNameDEBIT() {
        return "DEBIT";
    }

    public static String getServiceNameCREDIT() {
        return "CREDIT";
    }

    public static String getServiceNameINQUIRY() {
        return "INQUIRY";
    }

    public static String getServiceNameTestTPACall() {
        return "TestTPACall";
    }

    public static String getServiceNametpcallXOctet() {
        return "tpcall_x_octet";
    }

    public static String getServiceNametpcallXOctetZero() {
        return "tpcall_x_octet_zero";
    }

    public static String getServiceNametpcallXCommon() {
        return "tpcall_x_common";
    }

    public static String getServiceNametpcallXCType() {
        return "tpcall_x_c_type";
    }

    public static String getServiceNameTestTPCancel() {
        return "TestTPCancel";
    }

    public static String getServiceNameTestTPConnect() {
        return "TestTPConnect";
    }

    public static String getServiceNameTestTPConversation() {
        return "TestTPConversat";
    }

    public static String getServiceNameTestTPDiscon() {
        return "TestTPDiscon";
    }

    public static String getServiceNameTestTPConversa2() {
        return "TestTPConversat";
    }

    public static String getServiceNameTestTPFree() {
        return "TestTPFree";
    }

    public static String getServiceNameTestTPGetrply() {
        return "TestTPGetrply";
    }

    public static String getServiceNameTestTPRecv() {
        return "TestTPRecv";
    }

    public static String getServiceNameTestTPReturn() {
        return "TestTPReturnA";
    }

    public static String getServiceNameTestTPReturn2() {
        return "TestTPReturnA";
    }

    public static String getServiceNameTestTPSend() {
        return "TestTPSend";
    }

    public static String getServiceNameTestTPService() {
        return "TestTPService";
    }

    public static String getServiceNameTestTPUnadvertise() {
        return "TestTPUnadvertise";
    }

    public static String getServiceNameTX1() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public static String getServiceNameTX2() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public static String getServiceNameTestRollbackOnly() {
        return "TestRbkOnly";
    }

    public static String getServiceNameTTL() {
        return "TTL";
    }

    public native void tpadvertiseTestRollbackOnlyTpcallTPETIMEService();

    public native void tpadvertiseTestTpcallTPEOTYPEService();

    public native void tpadvertiseTestRollbackOnlyTpcallTPESVCFAILService();

    public native void tpadvertiseTestRollbackOnlyTprecvTPEVDISCONIMMService();

    public native void tpadvertiseTestRollbackOnlyTprecvTPEVSVCFAILService();

    public native void tpadvertiseTestRollbackOnlyNoTpreturnService();

    public native void tpadvertiseTTL();

    public native void tpadvertiseTestTPSendTPSendOnly();

    public static String getServiceNameTestTPSendTPSendOnly() {
        return "TestTPSend";
    }

    public native void tpadvertiseTestTPGetrplyOne();

    public native void tpadvertiseTestTPGetrplyTwo();

    public static String getServiceNameTestTPGetrplyOne() {
        return "TestTPGetAnyA";
    }

    public static String getServiceNameTestTPGetrplyTwo() {
        return "TestTPGetAnyB";
    }

    public native void tpadvertiseTestTPReturn3();

    public native void tpadvertiseTestTPReturn4();

    public static String getServiceNameTestRollbackOnly2() {
        return "TestRbkOnly2";
    }

    public static String getServiceNameNBF() {
        return "NBF";
    }

    public native void tpadvertiseTestNBF();
}
