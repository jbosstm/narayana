public class SimpleRecordOSVPlugin implements StateViewerInterface
{
    /**
     * A uid node of the type this viewer is registered against has been expanded.
     * @param os
     * @param type
     * @param manipulator
     * @param node
     * @throws ObjectStoreException
     */
    public void uidNodeExpanded(ObjectStore os,
                                String type,
                                ObjectStoreBrowserTreeManipulationInterface 
                                manipulator,
                                UidNode node,
                                StatePanel infoPanel)
        throws ObjectStoreException
    {
        // Do nothing
    }

    /**
     * An entry has been selected of the type this viewer is registered against.
     *
     * @param os
     * @param type
     * @param uid
     * @param entry
     * @param statePanel
     * @throws ObjectStoreException
     */
    public void entrySelected(ObjectStore os,
                              String type,
                              Uid uid,
                              ObjectStoreViewEntry entry,
                              StatePanel statePanel) 
        throws ObjectStoreException
    {
        SimpleRecord rec = new SimpleRecord();

        if ( rec.restore_state( os.read_committed(uid, type), ObjectType.ANDPERSISTENT ) )
            {
                statePanel.setData( “Value”, rec.getValue() );
            }
    }

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType()
    {
        return “/StateManager/AbstractRecord/SimpleRecord”;
    }
}