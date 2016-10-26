import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

@Transactional
public class SampleLockable implements Sample
{
    public SampleLockable (int init)
    {
        _isState = init;
    }
        
    @ReadLock
    public int value ()
    {
        return _isState;
    }

    @WriteLock
    public void increment ()
    {
        _isState++;
    }
        
    @WriteLock
    public void decrement ()
    {
        _isState--;
    }

    @State
    private int _isState;
}