public boolean save_state (OutputObjectState os, int ObjectType)
{
    if (!super.save_state(os, ObjectType))
        return false;

    try
        {
            os.packInt(numberOfElements);

            if (numberOfElements > 0)
                {
                    for (int i = 0; i < numberOfElements; i++)
                        os.packInt(elements[i]);
                }

            return true;
        }
    catch (IOException e)
        {
            return false;
        }
}