{
   ...
   org.omg.CosTransactions.Control c;
   org.omg.CosTransactions.Terminator t;
      org.omg.CosTransactions.PropagationContext pgtx;

   c = transFact.create(0);         // create top-level action

      pgtx = c.get_coordinator().get_txcontext();
   ...
   trans_object.operation(arg, pgtx);     // explicit propagation
   ...
   t = c.get_terminator();          // get terminator
   t.commit(false);              // so it can be used to commit
   ...
}