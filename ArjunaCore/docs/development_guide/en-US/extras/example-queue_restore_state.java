public boolean restore_state (InputObjectState os, int ObjectType)
{
    if (!super.restore_state(os, ObjectType))
        return false;

    try
        {
            numberOfElements = os.unpackInt();

            if (numberOfElements > 0)
                {
                    for (int i = 0; i < numberOfElements; i++)
                        elements[i] = os.unpackInt();
                }

            return true;
        }
    catch (IOException e)
        {
            return false;
        }
}
