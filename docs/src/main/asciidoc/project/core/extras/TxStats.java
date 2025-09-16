public class TxStats {
    /**
     * @return the number of transactions (top-level and nested) created so far.
     */

    public static int numberOfTransactions();

    /**
     * @return the number of nested (sub) transactions created so far.
     * <p>
     * public static int numberOfNestedTransactions();
     * <p>
     * /**
     * @return the number of transactions which have terminated with heuristic
     * outcomes.
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

    public static int numberOfInflightTransactions();

    /**
     * @return total number of transactions rolled back due to timeout.
     */

    public static int numberOfTimedOutTransactions();

    /**
     * @return the number of transactions rolled back by the application.
     */

    public static int numberOfApplicationRollbacks();

    /**
     * @return number of transactions rolled back by participants.
     */

    public static int numberOfResourceRollbacks();

    /**
     * Print the current information.
     */

    public static void printStatus(java.io.PrintWriter pw);

    /**
     * Returns the average time, in nanoseconds, it is taking to commit a transaction. This time is
     * measured from the moment the client calls commit until the transaction manager determines that the
     * commit attempt was successful (ie that all participants successfully committed). This includes cases where:
     *
     * <ul>
     *   <li>there are no transaction participants;
     *   <li>the transaction only contains readonly participants;
     * </ul>
     * <p>
     * The average will not be updated if any participants failed to commit.
     * <p>
     * Note that a small number of stuck transactions can skew the overall average. Similarly the average time
     * will be reduced if there are many transactions without participants or with only readonly participants.
     *
     * @return the average time, in nanoseconds, it has taken to commit a transaction.
     */
    long getAverageCommitTime();

    /**
     * Returns the number of transactions that have been rolled back due to internal system errors including
     * failure to create log storage and failure to write a transaction log. It does not include rollbacks
     * caused by resource failures.
     *
     * @return the number of transactions that been rolled back due to internal system errors
     */
    long getNumberOfSystemRollbacks();
}
