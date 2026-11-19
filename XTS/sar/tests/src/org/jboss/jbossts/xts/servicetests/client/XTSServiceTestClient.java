/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2009,
 * @author JBoss Inc.
 */

package org.jboss.jbossts.xts.servicetests.client;

import org.jboss.jbossts.xts.servicetests.generated.XTSServiceTestPortType;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;
import org.jboss.jbossts.xts.servicetests.generated.XTSServiceTestService;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;

/**
 * A convenience class used to invoke an XTS ServiceTest server.
 */

public class XTSServiceTestClient
{
    /**
     * all Clients employ a single service instance from which to clone port instances
     */
    final private static XTSServiceTestService service = new XTSServiceTestService();

    /**
     * each Client caches a ready-configured port
     */
    private XTSServiceTestPortType port;

    /**
     * create a client which can be used to make repeated invocations of the test web service methods
     */
    public XTSServiceTestClient()
    {
        // create a port and configure it with the WS context handler
        port = service.getXTSServiceTestPortType();
        List<Handler> handlerChain = new ArrayList<Handler>();
        handlerChain.add(new JaxWSHeaderContextProcessor());
        ((BindingProvider)port).getBinding().setHandlerChain(handlerChain);
    }

    /**
     * invoke a web service at a specified URL
     * @param serverURL
     * @param commands a list of operations to be performed by the web service
     * @return a list of zero or more results identifying the outcomes of the operations
     */
    public synchronized ResultsType serve(String serverURL, CommandsType commands)
    {

        Map<String, Object> requestProperties = ((BindingProvider)port).getRequestContext();
        requestProperties.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverURL);

        CommandsType commandsType = new CommandsType();
        return port.serve(commands);
    }
}
