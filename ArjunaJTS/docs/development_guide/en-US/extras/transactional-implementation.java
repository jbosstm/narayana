1    import Demo.*;
2    import org.omg.CosTransactions.*;
3    import com.arjuna.ats.jts.*;
4    import com.arjuna.orbportability.*;
5
6    public class DemoImplementation extends Demo.DemoInterfacePOA
7    {
8        public void work() throws DemoException
9        {
10           try
11           {
12
13               Control control = OTSManager.get_current().get_control();
14
15               Coordinator  coordinator = control.get_coordinator();
16               DemoResource resource    = new DemoResource();
17
18               ORBManager.getPOA().objectIsReady(resource);
19               coordinator.register_resource(resource);
20
21           }
22           catch (Exception e)
23           {
24               throw new DemoException();
25           }
26       }
27
28   }          
