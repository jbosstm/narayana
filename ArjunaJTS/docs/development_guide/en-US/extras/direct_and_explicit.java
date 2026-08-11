    ...
    org.omg.CosTransactions.Control c;
    org.omg.CosTransactions.Terminator t;
    org.omg.CosTransactions.Coordinator co;
    org.omg.CosTransactions.PropagationContext pgtx;

    c = TFactory.create(0);
    t = c.get_terminator();
    pgtx = c.get_coordinator().get_txcontext();
    ...