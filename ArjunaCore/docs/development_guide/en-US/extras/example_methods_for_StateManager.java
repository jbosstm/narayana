public boolean save_state ( OutputObjectState os, int ObjectType )
{
    if (!super.save_state(os, ObjectType))
        return false;

    try
        {
            os.packInt(A);
            os.packString(B);
            os.packFloat(C);

            return true;
        }
    catch (IOException e)
        {
            return false;
        }
}