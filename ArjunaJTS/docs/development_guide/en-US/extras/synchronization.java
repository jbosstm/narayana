interface Synchronization : TransactionalObject
{
   void before_completion ();
   void after_completion (in Status s);
};