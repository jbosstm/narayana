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
package org.jboss.narayana.blacktie.jatmibroker.core.conf;

import java.util.Properties;

public class AtmiBrokerEnvXML {
    private Properties prop;

    public AtmiBrokerEnvXML() {
        prop = new Properties();
    }

    public Properties getProperties() throws ConfigurationException {
        XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);

        // define BTStompAdmin and BTDomainAdmin
        prop.put("blacktie.BTStompAdmin.server", "jboss");
        prop.put("blacktie.BTStompAdmin.conversational", false);

        prop.put("blacktie.BTDomainAdmin.server", "jboss");
        prop.put("blacktie.BTDomainAdmin.conversational", false);
        
        // just for socket server test
        //prop.setProperty("blacktie.java.socketserver.port", "12340");

        return prop;
    }
}
