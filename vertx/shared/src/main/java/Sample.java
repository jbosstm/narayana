import org.jboss.stm.annotations.Transactional;

@Transactional
public interface Sample
{
   public void increment ();
   public void decrement ();
       
   public int value ();
}