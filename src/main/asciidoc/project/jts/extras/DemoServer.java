import java.io.*;

import com.arjuna.orbportability.*;

public class DemoServer {
    public static void main(String[] args) {
        try {
            ORB myORB = ORB.getInstance("test").initORB(args, null);
            RootOA myOA = OA.getRootOA(myORB).myORB.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            DemoImplementation obj = new DemoImplementation();

            myOA.objectIsReady(obj);

            Services serv = new Services(myORB);
            serv.registerService(myOA.corbaReference(obj), "DemoObjReference", null);

            System.out.println("Object published.");

            myOA.run();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
