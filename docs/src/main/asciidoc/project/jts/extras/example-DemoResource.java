import org.omg.CosTransactions.*;
import org.omg.CORBA.SystemException;

public class DemoResource extends org.omg.CosTransactions.ResourcePOA {
    public Vote prepare() throws HeuristicMixed, HeuristicHazard, SystemException {
        System.out.println("prepare called");

        return Vote.VoteCommit;
    }

    public void rollback() throws HeuristicCommit, HeuristicMixed, HeuristicHazard, SystemException {
        System.out.println("rollback called");
    }

    public void commit() throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard, SystemException {
        System.out.println("commit called");
    }

    public void commit_one_phase() throws HeuristicHazard, SystemException {
        System.out.println("commit_one_phase called");
    }

    public void forget() throws SystemException {
        System.out.println("forget called");
    }
}
