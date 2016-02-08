package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ResponseException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TestTPConversation;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPConversationService implements Service {
    private static final Logger log = LogManager.getLogger(TPConversationService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("testTPConversation_service");
        boolean fail = false;

        if (TestTPConversation.strcmp((X_OCTET) svcinfo.getBuffer(), "conversate") != 0) {
            if (svcinfo.getBuffer() != null) {
                log.error("Got invalid data %s" + new String(((X_OCTET) svcinfo.getBuffer()).getByteArray()));
            } else {
                log.error("GOT A NULL");
            }
            fail = true;
        } else {
            long revent = 0;
            log.info("Chatting");
            for (int i = 0; i < TestTPConversation.interationCount; i++) {
                byte[] bytes = ("hi" + i).getBytes();

                X_OCTET sendbuf = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
                sendbuf.setByteArray(bytes);
                // btlogger((char*) "testTPConversation_service:%s:",
                // sendbuf);
                int result = svcinfo.getSession().tpsend(sendbuf, Connection.TPRECVONLY);
                if (result != -1) {
                    try {
                        svcinfo.getSession().tprecv(0);
                        fail = true;
                        break;
                    } catch (ResponseException e) {
                        if (e.getEvent() == Connection.TPEV_SENDONLY) {
                            Buffer rcvbuf = e.getReceived();
                            if (TestTPConversation.strcmp("yo" + i, rcvbuf) != 0) {
                                fail = true;
                                break;
                            }
                        } else {
                            fail = true;
                            break;
                        }
                    } catch (ConnectionException e) {
                        fail = true;
                        break;
                    }
                } else {
                    fail = true;
                    break;
                }
            }
            log.info("Chatted");
        }

        if (fail) {
            return new Response((short) Connection.TPESVCFAIL, 0, null, 0);
        } else {
            byte[] bytes = ("hi" + TestTPConversation.interationCount).getBytes();
            X_OCTET sendbuf = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
            sendbuf.setByteArray(bytes);
            return new Response(Connection.TPSUCCESS, 0, sendbuf, 0);
        }
    }
}