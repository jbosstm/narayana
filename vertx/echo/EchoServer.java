
import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.streams.Pump;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EchoServer extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new EchoServer());
  }

  @Override
  public void start() throws Exception {

    vertx.createNetServer().connectHandler(sock -> {

      // Create a pump
      Pump.pump(sock, sock).start();

    }).listen(1234);

    System.out.println("Echo server is now listening");

  }
}
