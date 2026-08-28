1    import java.io.*;
2    import com.arjuna.orbportability.*;
3
4    public class DemoServer
5    {
6        public static void main (String[] args)
7        {
8            try
9            {
10               ORB myORB = ORB.getInstance("test").initORB(args, null);
11               RootOA myOA = OA.getRootOA(myORB).myORB.initOA();
12
13               ORBManager.setORB(myORB);
14                   ORBManager.setPOA(myOA);
15
16                   DemoImplementation obj = new DemoImplementation();
17               
18               myOA.objectIsReady(obj);
19               
20               Services serv = new Services(myORB);
21               serv.registerService(myOA.corbaReference(obj), "DemoObjReference", null);
22
23               System.out.println("Object published.");
24
25               myOA.run();
26           }
27           catch (Exception e)
28           {
29               System.err.println(e);
30           }
31       }
32   }
