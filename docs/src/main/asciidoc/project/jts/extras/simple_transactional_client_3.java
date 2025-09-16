{
  ...
  org.omg.CosTransactions.Control c;
  org.omg.CosTransactions.Terminator t;
  org.omg.CosTransactions.PropagationContext pgtx;

  // create top-level action
  c = transFact.create(0);
  pgtx = c.get_coordinator().get_txcontext();

  // set implicit context
  current.resume(c);
  ...
  // explicit propagation
  trans_object.operation(arg, pgtx);
  // implicit propagation
  trans_object2.operation(arg);
  ...
  // oops! rollback
  current.rollback();
  ...
}
