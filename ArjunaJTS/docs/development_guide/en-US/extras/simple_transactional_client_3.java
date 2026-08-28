{
   ...
   org.omg.CosTransactions.Control c;
   org.omg.CosTransactions.Terminator t;
      org.omg.CosTransactions.PropagationContext pgtx;

   c = transFact.create(0);         // create top-level action
      pgtx = c.get_coordinator().get_txcontext();

   current.resume(c);            // set implicit context
   ...
   trans_object.operation(arg, pgtx);     // explicit propagation
   trans_object2.operation(arg);       // implicit propagation
   ...
   current.rollback();           // oops! rollback
   ...
}