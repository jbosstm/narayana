public class TxStats
{
    /**
     * @return the number of transactions (top-level and nested) created so far.
     */

    public static int numberOfTransactions();

    /**
     * @return the number of nested (sub) transactions created so far.
     *

     public static int numberOfNestedTransactions();

     /**
     * @return the number of transactions which have terminated with heuristic
     *         outcomes.
     */

    public static int numberOfHeuristics();
    /**
     * @return the number of committed transactions.
     */

    public static int numberOfCommittedTransactions();

    /**
     * @return the total number of transactions which have rolled back.
     */

    public static int numberOfAbortedTransactions();  

    /**
     * @return total number of inflight (active) transactions.
     */  

    public static int numberOfInflightTransactions ();

    /**
     * @return total number of transactions rolled back due to timeout.
     */  

    public static int numberOfTimedOutTransactions ();
    /**
     * @return the number of transactions rolled back by the application.
     */  

    public static int numberOfApplicationRollbacks ();

    /**
     * @return number of transactions rolled back by participants.
     */  

    public static int numberOfResourceRollbacks ();

    /**
     * Print the current information.
     */  

    public static void printStatus(java.io.PrintWriter pw);
}