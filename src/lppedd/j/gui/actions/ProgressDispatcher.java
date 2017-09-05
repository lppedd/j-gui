package lppedd.j.gui.actions;

/**
 * @author Edoardo Luppi
 */
public interface ProgressDispatcher
{
   public void updateProgress(final int progress);

   public void updateTotal(final int total);
   
   public void updateText(final String text);
}
