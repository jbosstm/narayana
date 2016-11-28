module java.transaction { 
  requires transitive java.rmi;
  requires transitive java.sql;
  exports javax.transaction; 
}