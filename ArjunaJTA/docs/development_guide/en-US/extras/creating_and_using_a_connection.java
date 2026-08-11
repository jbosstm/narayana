Statement stmt = conn.createStatement();

try
    {
        stmt.executeUpdate("CREATE TABLE test_table (a INTEGER,b INTEGER)");
    }
catch (SQLException e)
    {
        // table already exists
    }

stmt.executeUpdate("INSERT INTO test_table (a, b) VALUES (1,2)");

ResultSet res1 = stmt.executeQuery("SELECT * FROM test_table");        
