public Vote prepare () throws WrongStateException, SystemException
{
  // Some participant logic here

  if(/* some condition based on the outcome of the business logic */)
  {
    // Vote to confirm
    return new com.arjuna.wst.Prepared();
  }
  else if(/*another condition based on the outcome of the business logic*/)
  {
    // Resign
    return new com.arjuna.wst.ReadOnly();
  }
  else
  {
    // Vote to cancel
    return new com.arjuna.wst.Aborted();
  }
}