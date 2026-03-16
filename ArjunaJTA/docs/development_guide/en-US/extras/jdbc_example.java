public class JDBCTest
{
    public static void main (String[] args)
    {
        /*
         */

        Connection conn = null;
        Connection conn2 = null;
        Statement stmt = null;        // non-tx statement
        Statement stmtx = null;  // will be a tx-statement
        Properties dbProperties = new Properties();

        try
            {
                System.out.println("\nCreating connection to database: "+url);

                /*
                 * Create conn and conn2 so that they are bound to the JBossTS
                 * transactional JDBC driver. The details of how to do this will
                 * depend on your environment, the database you wish to use and
                 * whether or not you want to use the Direct or JNDI approach. See
                 * the appropriate chapter in the JTA Programmers Guide.
                 */

                stmt = conn.createStatement();  // non-tx statement

                try
                    {
                        stmt.executeUpdate("DROP TABLE test_table");
                        stmt.executeUpdate("DROP TABLE test_table2");
                    }
                catch (Exception e)
                    {
                        // assume not in database.
                    }

                try
                    {
                        stmt.executeUpdate("CREATE TABLE test_table (a INTEGER,b INTEGER)");
                        stmt.executeUpdate("CREATE TABLE test_table2 (a INTEGER,b INTEGER)");
                    }
                catch (Exception e)
                    {
                    }

                try
                    {
                        System.out.println("Starting top-level transaction.");

                        com.arjuna.ats.jta.UserTransaction.userTransaction().begin();

                        stmtx = conn.createStatement(); // will be a tx-statement

                        System.out.println("\nAdding entries to table 1.");

                        stmtx.executeUpdate("INSERT INTO test_table (a, b) VALUES (1,2)");

                        ResultSet res1 = null;

                        System.out.println("\nInspecting table 1.");

                        res1 = stmtx.executeQuery("SELECT * FROM test_table");
                        while (res1.next())
                            {
                                System.out.println("Column 1: "+res1.getInt(1));
                                System.out.println("Column 2: "+res1.getInt(2));
                            }

                        System.out.println("\nAdding entries to table 2.");

                        stmtx.executeUpdate("INSERT INTO test_table2 (a, b) VALUES (3,4)");
                        res1 = stmtx.executeQuery("SELECT * FROM test_table2");
                        System.out.println("\nInspecting table 2.");

                        while (res1.next())
                            {
                                System.out.println("Column 1: "+res1.getInt(1));
                                System.out.println("Column 2: "+res1.getInt(2));
                            }
                        System.out.print("\nNow attempting to rollback changes.");
                        com.arjuna.ats.jta.UserTransaction.userTransaction().rollback();

                        com.arjuna.ats.jta.UserTransaction.userTransaction().begin();
                        stmtx = conn.createStatement();
                        ResultSet res2 = null;

                        System.out.println("\nNow checking state of table 1.");

                        res2 = stmtx.executeQuery("SELECT * FROM test_table");
                        while (res2.next())
                            {
                                System.out.println("Column 1: "+res2.getInt(1));
                                System.out.println("Column 2: "+res2.getInt(2));
                            }

                        System.out.println("\nNow checking state of table 2.");

                        stmtx = conn.createStatement();
                        res2 = stmtx.executeQuery("SELECT * FROM test_table2");
                        while (res2.next())
                            {
                                System.out.println("Column 1: "+res2.getInt(1));
                                System.out.println("Column 2: "+res2.getInt(2));
                            }

                        com.arjuna.ats.jta.UserTransaction.userTransaction().commit(true);
                    }
                catch (Exception ex)
                    {
                        ex.printStackTrace();
                        System.exit(0);
                    }
            }
        catch (Exception sysEx)
            {
                sysEx.printStackTrace();
                System.exit(0);
            }
    }