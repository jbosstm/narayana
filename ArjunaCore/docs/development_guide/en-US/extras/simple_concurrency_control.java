public boolean op1 (...)
{  
    if (setlock (new Lock(LockMode.WRITE) == LockResult.GRANTED)
    {
        // actual state change operations follow 
        ...
    }
}