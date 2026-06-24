public boolean set (int index, int value)
{
   boolean result = false;
   AtomicAction A = new AtomicAction();

   A.begin();

   // We need to set a WRITE lock as we want to modify the state.

   if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
   {
      elements[index] = value;
      if ((value > 0) &&(index > highestIndex
         highestIndex = index;
      A.commit(true);
      result = true;
   }
   else
      A.rollback();

   return result;
}