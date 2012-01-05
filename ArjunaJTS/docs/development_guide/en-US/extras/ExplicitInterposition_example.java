public boolean increment (Control control)
{
    ExplicitInterposition inter = new ExplicitInterposition();

    try
        {
            inter.registerTransaction(control);
        }
    catch (Exception e)
        {
            return false;
        }

    // do real work

    inter.unregisterTransaction();  // should catch exceptions!

    // return value dependant upon outcome
}