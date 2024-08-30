import Demo.*;
import org.omg.CosTransactions.*;
import com.arjuna.ats.jts.*;
import com.arjuna.orbportability.*;

public class DemoImplementation extends Demo.DemoInterfacePOA {
    public void work() throws DemoException {
        try {

            Control control = OTSManager.get_current().get_control();

            Coordinator coordinator = control.get_coordinator();
            DemoResource resource = new DemoResource();

            ORBManager.getPOA().objectIsReady(resource);
            coordinator.register_resource(resource);

        } catch (Exception e) {
            throw new DemoException();
        }
    }
}          
