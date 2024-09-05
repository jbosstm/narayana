{
  ...
  org.omg.CosTransactions.Control c;
  org.omg.CosTransactions.Terminator t;
  org.omg.CosTransactions.PropagationContext pgtx;

  // create top-level action
  c = transFact.create(0);

  pgtx = c.get_coordinator().get_txcontext();
  ...
  // explicit propagation
  trans_object.operation(arg, pgtx);
  ...
  // get terminator
  t = c.get_terminator();
  // so it can be used to commit
  t.commit(false);
  ...
}
