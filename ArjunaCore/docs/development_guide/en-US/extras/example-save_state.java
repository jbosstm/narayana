public boolean save_state(OutputObjectState o)
{
    if (!super.save_state(o))
        return false;

    try
        {
            o.packInt(A);
            o.packInt(B);
            o.packInt(C));
}
catch (Exception e)
    {
        return false;
    }

return true;
}
