public int get (int index)  // assume -1 means error
{
   AtomicAction A = new AtomicAction();

   A.begin();

   // We only need a READ lock as the state is unchanged.

   if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
   {
      A.commit(true);

             return elements[index];
   }
   else
      A.rollback();

   return -1;
}