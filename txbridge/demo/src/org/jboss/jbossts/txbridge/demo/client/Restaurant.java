package org.jboss.jbossts.txbridge.demo.client;

import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "jaxws-handlers-client.xml") // relative path from the class file
public interface Restaurant
{
    public void bookSeats(@WebParam(name = "how_many", partName = "how_many")int how_many);
}
