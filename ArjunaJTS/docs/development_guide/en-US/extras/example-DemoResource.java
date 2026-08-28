1    import org.omg.CosTransactions.*;
2    import org.omg.CORBA .SystemException;
3
4    public class DemoResource extends  org.omg.CosTransactions .ResourcePOA
5    {
6        public Vote prepare() throws HeuristicMixed, HeuristicHazard,
7        SystemException
8        {
9            System.out.println("prepare called");
10
11           return Vote.VoteCommit;
12       }
13
14       public void rollback() throws HeuristicCommit, HeuristicMixed,
15       HeuristicHazard, SystemException
16       {
17           System.out.println("rollback called");
18       }
19
20       public void commit() throws NotPrepared, HeuristicRollback,
21       HeuristicMixed, HeuristicHazard, SystemException
22       {
23           System.out.println("commit called");
24       }
25
26       public void commit_one_phase() throws HeuristicHazard, SystemException
27       {
28           System.out.println("commit_one_phase called");
29       }
30
31       public void forget() throws SystemException
32       {
33           System.out.println("forget called");
34       }
35   }    