Properties dbProps = new Properties();

dbProps.setProperty(TransactionalDriver.userName, "user");
dbProps.setProperty(TransactionalDriver.password, "password");

// the driver uses its own JNDI context info, remember to set it up:
jdbcPropertyManager.propertyManager.setProperty(
                                                "Context.INITIAL_CONTEXT_FACTORY", initialCtx);
jdbcPropertyManager.propertyManager.setProperty(
                                                "Context.PROVIDER_URL", myUrl);

TransactionalDriver arjunaJDBCDriver = new TransactionalDriver();
Connection connection = arjunaJDBCDriver.connect("jdbc:arjuna:jdbc/foo", dbProps);
