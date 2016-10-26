
import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.Container;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 * and others
 */
public class EchoClient extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new EchoClient());
  }

  @Override
  public void start() throws Exception {
    vertx.createNetClient().connect(1234, "localhost", res -> {

      if (res.succeeded()) {
        NetSocket socket = res.result();
        socket.handler(buffer -> {
          System.out.println("Net client receiving: " + buffer.toString("UTF-8"));
        });

        // Now send some data
        for (int i = 0; i < 10; i++) {
          String str = "hello " + i + "\n";
          System.out.println("Net client sending: " + str);
          socket.write(str);
        }

	  /*
	   * If you are running this for the first time then leave as is.
	   * If you are running this more than once and want clients to share the STM objects between
	   * address spaces then go into the ObjectStore dir and look for the Uid that represents the state
	   * you want to share. Then uncomment the Uid line below and replace the Uid in quotes with the Uid
	   * you have selected. Uncomment the other obj1 creation line and comment out the original.
	   *
	   * If you want to see how this might work then just go with the example state in the ObjectStore
	   * shipped as part of this example and uncomment the lines.
	   */
	  
	  /*
	   * STM states are identified by Uids in the ObjectStore. This is an example.
	   */

	  // Modify this line if sharing state and uncomment.
	  // Uid u = new Uid("0:ffffc0a80003:c915:529f59de:1");

	  Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);

	  // Modify this line if sharing state and uncomment.
	  // Sample obj1 = theContainer.clone(new SampleLockable(10), u);

	  Sample obj1 = theContainer.create(new SampleLockable(10));

	System.out.println("Object name: "+theContainer.getIdentifier(obj1));

          //Now send some data
          for (int i = 0; i < 10; i++) {
	      AtomicAction A = new AtomicAction();

	      A.begin();
	      obj1.increment();

	      String str = "hello" + obj1.value() + "\n";
            System.out.print("Net client sending: " + str);
            socket.write(str);

	    A.commit();
          }
      } else {
        System.out.println("Failed to connect " + res.cause());
      }
    });
  }

    @Transactional
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    @Transactional
    public class SampleLockable implements Sample
    {
        public SampleLockable (int init)
        {
            _isState = init;
        }
        
        @ReadLock
        public int value ()
        {
            return _isState;
        }

        @WriteLock
        public void increment ()
        {
            _isState++;
        }
        
        @WriteLock
        public void decrement ()
        {
            _isState--;
        }

        @State
        private int _isState;
    }
}
