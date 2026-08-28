package com.arjuna.webservices11.wsaddr;

import com.arjuna.webservices11.wsaddr.NativeEndpointReference;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * helper class to allow transformation of W3CEndpointreference instances to
 * internal types which are not opaque
 */
public class EndpointHelper
{
    public static <T extends EndpointReference> T transform(Class<T> clazz, EndpointReference epr)
    {
       assert epr != null;
       if (clazz.isAssignableFrom(W3CEndpointReference.class))
       {
          if (epr instanceof W3CEndpointReference)
          {
             return (T)epr;
          }
          else if (epr instanceof NativeEndpointReference)
          {
             return (T)W3CEndpointReference.readFrom(getSourceFromEndpointReference(epr));
          }
          else
          {
             throw new WebServiceException("Unsupported EndpointReference: " + epr);
          }
       }
       else if (clazz.isAssignableFrom(NativeEndpointReference.class))
       {
          return (T)NativeEndpointReference.readFrom(getSourceFromEndpointReference(epr));
       }
       //transformations from different types of EndpointReference could be supported in future...

       throw new WebServiceException("EndpointReference of type " + clazz + " not supported.");
    }

    private static Source getSourceFromEndpointReference(EndpointReference epr)
    {
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       StreamResult result = new StreamResult(outputStream);
       epr.writeTo(result);
       return new StreamSource(new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
