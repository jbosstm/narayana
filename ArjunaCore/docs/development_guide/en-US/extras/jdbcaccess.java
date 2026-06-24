public interface JDBCAccess
{
    public Connection getConnection () throws SQLException;
    public void putConnection (Connection conn) throws SQLException;
    public void initialise (Object[] objName);
}