import Demo.*;

import java.io.*;

import com.arjuna.orbportability.*;
import com.arjuna.ats.jts.*;
import org.omg.CosTransactions.*;
import org.omg.*;

public class DemoClient {
    public static void main(String[] args) {
        try {
            ORB myORB = ORB.getInstance("test").initORB(args, null);
            RootOA myOA = OA.getRootOA(myORB).myORB.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            Services serv = new Services(myORB);
            DemoInterface d = (DemoInterface) DemoInterfaceHelper.narrow(serv.getService("DemoObjReference"));

            OTS.get_current().begin();

            d.work();

            OTS.get_current().commit(true);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
