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
package org.jboss.narayana.blacktie.stompconnect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.stomp.jms.StompConnect;

public class StompConnectService implements StompConnectServiceMBean {
    private static final Logger log = LogManager.getLogger(StompConnectService.class);
    private StompConnect connect;
    private int port = 61613;
    private String connectionFactoryName = "java:/JmsXA";

    public void start() throws NamingException, IOException, URISyntaxException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        String bindAddress = System.getProperty("jboss.bind.address", "127.0.0.1");
        if (bindAddress.equals("") || bindAddress.equals("0.0.0.0")) {
            bindAddress = "127.0.0.1";
        }
        String uri = "tcp://" + bindAddress + ":" + port;
        log.info("Starting StompConnectMBeanImpl: " + uri);
        connect = new StompConnect();
        connect.setUri(uri);
        connect.setXAConnectionFactoryName(connectionFactoryName);
        connect.start();
        log.info("Started StompConnectMBeanImpl: " + connect.getUri());
    }

    public void stop() throws InterruptedException, IOException, JMSException, URISyntaxException {
        connect.stop();
    }

    public void setPort(String port) {
        if (port != null) {
            this.port = Integer.parseInt(port);
        } else {
            this.port = 0;
        }
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }
}
