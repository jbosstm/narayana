1    import Demo.*;
2    import java.io.*;
3    import com.arjuna.orbportability.*;
4    import com.arjuna.ats.jts.*;
5    import org.omg.CosTransactions.*;
6    import org.omg.*;
7
8    public class DemoClient
9    {
10       public static void main(String[] args)
11       {
12           try
13           {
14               ORB myORB = ORB.getInstance("test").initORB(args, null);
15               RootOA myOA = OA.getRootOA(myORB).myORB.initOA();
16
17               ORBManager.setORB(myORB);
18               ORBManager.setPOA(myOA);
19               
20                   Services serv = new Services(myORB);
21               DemoInterface d = (DemoInterface) DemoInterfaceHelper.narrow(serv.getService("DemoObjReference"));
22               
23               OTS.get_current().begin();
24
25               d.work();
26
27               OTS.get_current().commit(true);
28           }
29           catch (Exception e)
30           {
31               System.err.println(e);
32           }
33       }
34   }
