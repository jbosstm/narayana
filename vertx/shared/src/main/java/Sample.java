/*
 * SPDX short identifier: Apache-2.0
 */
import org.jboss.stm.annotations.Transactional;

@Transactional
public interface Sample
{
   public void increment ();
   public void decrement ();
       
   public int value ();
}