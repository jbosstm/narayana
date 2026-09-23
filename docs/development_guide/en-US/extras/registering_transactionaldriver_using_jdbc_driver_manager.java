Properties p = System.getProperties(); 

switch (dbType)
{
case MYSQL:
    p.put("jdbc.drivers", "com.mysql.jdbc.Driver"); 
    break;
case PGSQL:
    p.put("jdbc.drivers", "org.postgresql.Driver"); 
    break;
}

System.setProperties(p);
