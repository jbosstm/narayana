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

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public class AtmiBrokerEnvXMLTest extends TestCase {

    public void testEnv() throws Exception {
        AtmiBrokerEnvXML envXml = new AtmiBrokerEnvXML();
        Properties prop = envXml.getProperties();

        String domain = "fooapp";
        String transid = "TransactionManagerService.OTS";
        String args = "2";
        String arg1 = "-ORBInitRef";
        String arg2 = "NameService=corbaloc::";
        String arg3 = ":3528/NameService";

        assertTrue(domain.equals(prop.getProperty("blacktie.domain.name")));
        assertTrue(transid.equals(prop.getProperty("blacktie.trans.factoryid")));
        assertTrue(args.equals(prop.getProperty("blacktie.orb.args")));
        assertTrue(arg1.equals(prop.getProperty("blacktie.orb.arg.1")));
        assertTrue(((String) prop.getProperty("blacktie.orb.arg.2")).startsWith(arg2));
        assertTrue(((String) prop.getProperty("blacktie.orb.arg.2")).endsWith(arg3));

        List<Server> serverLaunchers = (List<Server>) prop.get("blacktie.domain.serverLaunchers");
        boolean found = false;
        Iterator<Server> iterator = serverLaunchers.iterator();
        while (iterator.hasNext()) {
            Server next = iterator.next();
            assertTrue("myserv".equals(next.getName()));
            List<Machine> localMachinesList = next.getLocalMachine();
            assertTrue(localMachinesList.size() == 2);

            for (int i = 0; i < localMachinesList.size(); i++) {
                String hostname = InetAddress.getLocalHost().getHostName();
                String elementHostname = localMachinesList.get(i).getHostname();
                String ipAddress = InetAddress.getLoopbackAddress().getHostAddress();
                String elementIpAddress = localMachinesList.get(i).getIpAddress();
                if (elementIpAddress != null) {
                    assertTrue(elementIpAddress.equals(ipAddress));
                }
                if (elementHostname != null) {
                    assertTrue(elementHostname.equals(hostname));
                }
            }

            assertTrue(localMachinesList.get(0).getArgLine().equals("foo"));

            assertTrue(localMachinesList.get(0).getId() != null);
            assertTrue(localMachinesList.get(0).getPathToExecutable().equals("notExist"));
            assertTrue(localMachinesList.get(0).getWorkingDirectory().equals("."));
            assertTrue(localMachinesList.get(0).getServerId() == 1);

            // Next index
            assertTrue(localMachinesList.get(1).getArgLine().equals("foo"));
            assertTrue(localMachinesList.get(1).getId() != null);
            assertTrue(localMachinesList.get(1).getPathToExecutable().equals("notExist"));
            assertTrue(localMachinesList.get(1).getWorkingDirectory().equals("."));
            assertTrue(localMachinesList.get(1).getServerId() == 2);
        }
    }
}
