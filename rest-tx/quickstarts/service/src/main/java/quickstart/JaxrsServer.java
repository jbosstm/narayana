package quickstart;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Start a simple embedded web server for hosting web services that will participate in
 * REST Atomic transactions.
 *
 * We use Grizzly since it is the reference implementation and it supports JAX-RS.
 * Using JAX-RS make our web service example is trivial to implement.
 */
public class JaxrsServer {
   private static SelectorThread threadSelector = null;

/*   public static void startTJWS(String host, int port) {
        org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer server;
        server = new org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer();
        server.setPort(port);
        server.start();

        org.jboss.resteasy.spi.Registry registry = server.getDeployment().getRegistry();

        registry.addPerRequestResource(TransactionAwareResource.class);
    }*/

    public static void startGrizzly(String host, int port) {
        final URI baseUri= UriBuilder.fromUri("http://" + host + ':' + port + '/').build();
        final Map<String, String> initParams = new HashMap<String, String>();
        String packages = TransactionAwareResource.class.getPackage().getName();
        initParams.put("com.sun.jersey.config.property.packages", packages);

        try {
            threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServer(String host, int port) {
         startGrizzly(host, port);
    }

    public static void stopServer() {
        if (threadSelector != null)
            threadSelector.stopEndpoint();

        threadSelector = null;
    }
}
