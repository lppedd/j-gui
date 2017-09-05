package lppedd.j.gui.actions;

/**
 * @author Edoardo Luppi
 */
public interface Action
{
   public void run();

   public void interrupt();
   
   public boolean isInterrupted();
}
