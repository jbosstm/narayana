XADataSource ds = MyXADataSource();
Hashtable env = new Hashtable();
String initialCtx = PropertyManager.getProperty("Context.INITIAL_CONTEXT_FACTORY");

env.put(Context.INITIAL_CONTEXT_FACTORY, initialCtx);

initialContext ctx = new InitialContext(env);

ctx.bind("jdbc/foo", ds);
