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
package org.jboss.narayana.blacktie.jatmibroker.core.transport;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class NamingServiceClient {
    private static final Logger log = LogManager.getLogger(NamingServiceClient.class);

    @Ignore
    public void test() throws InvalidName {
        String[] args = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=corbaloc::localhost:3528/NameService";
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "3528");
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        ORB orb = ORB.init(args, props);
        NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        BindingListHolder bl = new BindingListHolder();
        BindingIteratorHolder blIt = new BindingIteratorHolder();
        nc.list(1000, bl, blIt);
        Binding bindings[] = bl.value;
        List<String> toResolve = new ArrayList<String>();
        toResolve.add("TransactionManagerService");
        for (int i = 0; i < bindings.length; i++) {

            int lastIx = bindings[i].binding_name.length - 1;

            // check to see if this is a naming context
            if (bindings[i].binding_type == BindingType.ncontext) {
                log.info("Context: " + bindings[i].binding_name[lastIx].id);
            } else {
                log.info("Object: " + bindings[i].binding_name[lastIx].id);
            }
            toResolve.remove(bindings[i].binding_name[lastIx].id);
        }
        assertTrue(toResolve.isEmpty());
    }
}
